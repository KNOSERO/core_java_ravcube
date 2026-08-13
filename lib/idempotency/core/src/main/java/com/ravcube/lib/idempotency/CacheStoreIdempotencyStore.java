package com.ravcube.lib.idempotency;

import com.ravcube.lib.cache.CacheStore;
import io.github.josipmusa.idempotency.core.AcquireResult;
import io.github.josipmusa.idempotency.core.IdempotencyContext;
import io.github.josipmusa.idempotency.core.IdempotencyStore;
import io.github.josipmusa.idempotency.core.StoredResponse;
import java.io.Serializable;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CacheStoreIdempotencyStore implements IdempotencyStore {

    private static final Duration FAILED_ENTRY_TTL = Duration.ofMinutes(1);

    private final CacheStore cacheStore;
    private final Clock clock;
    private final String keyPrefix;

    public CacheStoreIdempotencyStore(CacheStore cacheStore, String keyPrefix) {
        this(cacheStore, Clock.systemUTC(), keyPrefix);
    }

    CacheStoreIdempotencyStore(CacheStore cacheStore, Clock clock, String keyPrefix) {
        this.cacheStore = Objects.requireNonNull(cacheStore, "cacheStore must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.keyPrefix = normalizeKeyPrefix(keyPrefix);
    }

    @Override
    public synchronized AcquireResult tryAcquire(IdempotencyContext context) {
        IdempotencyContext idempotencyContext = Objects.requireNonNull(context, "context must not be null");
        String cacheKey = cacheKey(idempotencyContext.key());
        Entry existing = cacheStore.get(cacheKey, Entry.class).orElse(null);

        if (existing == null || existing.canBeReplaced(clock.instant())) {
            cacheStore.put(cacheKey, Entry.inProgress(idempotencyContext, clock.instant()), idempotencyContext.ttl());
            return AcquireResult.acquired();
        }

        if (existing.isComplete()) {
            if (!existing.requestFingerprint().equals(idempotencyContext.requestFingerprint())) {
                return AcquireResult.fingerprintMismatch(
                        existing.requestFingerprint(),
                        idempotencyContext.requestFingerprint()
                );
            }
            return AcquireResult.duplicate(existing.toStoredResponse());
        }

        return AcquireResult.lockTimeout(idempotencyContext.key());
    }

    @Override
    public synchronized void complete(String key, StoredResponse response, Duration ttl) {
        String cacheKey = cacheKey(key);
        Entry existing = cacheStore.get(cacheKey, Entry.class).orElseThrow(
                () -> new IllegalStateException("Idempotency entry does not exist for key " + key)
        );

        cacheStore.put(cacheKey, existing.complete(response), ttl);
    }

    @Override
    public synchronized void release(String key) {
        String cacheKey = cacheKey(key);
        Entry existing = cacheStore.get(cacheKey, Entry.class).orElse(null);
        if (existing != null && existing.isInProgress()) {
            cacheStore.put(cacheKey, existing.failed(), FAILED_ENTRY_TTL);
        }
    }

    @Override
    public synchronized void extendLock(String key, Duration ttl) {
        String cacheKey = cacheKey(key);
        Entry existing = cacheStore.get(cacheKey, Entry.class).orElse(null);
        if (existing != null && existing.isInProgress()) {
            cacheStore.put(cacheKey, existing.extendLock(clock.instant(), ttl), existing.ttl());
        }
    }

    @Override
    public int purgeExpired() {
        return 0;
    }

    private String cacheKey(String key) {
        return keyPrefix + Objects.requireNonNull(key, "key must not be null").trim();
    }

    private String normalizeKeyPrefix(String keyPrefix) {
        String prefix = Objects.requireNonNullElse(keyPrefix, "idempotency").trim();
        if (prefix.isEmpty()) {
            prefix = "idempotency";
        }
        return prefix.endsWith(":") ? prefix : prefix + ":";
    }

    private enum Status {
        IN_PROGRESS,
        COMPLETE,
        FAILED
    }

    private record Entry(
            Status status,
            int statusCode,
            Map<String, List<String>> headers,
            byte[] body,
            Instant completedAt,
            Instant lockExpiresAt,
            Duration ttl,
            String requestFingerprint
    ) implements Serializable {

        static Entry inProgress(IdempotencyContext context, Instant now) {
            return new Entry(
                    Status.IN_PROGRESS,
                    0,
                    Map.of(),
                    new byte[0],
                    null,
                    now.plus(context.lockTimeout()),
                    context.ttl(),
                    context.requestFingerprint()
            );
        }

        Entry complete(StoredResponse response) {
            return new Entry(
                    Status.COMPLETE,
                    response.statusCode(),
                    response.headers(),
                    response.body(),
                    response.completedAt(),
                    null,
                    ttl,
                    requestFingerprint
            );
        }

        Entry failed() {
            return new Entry(Status.FAILED, statusCode, headers, body, completedAt, null, FAILED_ENTRY_TTL, requestFingerprint);
        }

        Entry extendLock(Instant now, Duration ttl) {
            return new Entry(status, statusCode, headers, body, completedAt, now.plus(ttl), this.ttl, requestFingerprint);
        }

        boolean canBeReplaced(Instant now) {
            return status == Status.FAILED || (lockExpiresAt != null && lockExpiresAt.isBefore(now));
        }

        boolean isInProgress() {
            return status == Status.IN_PROGRESS;
        }

        boolean isComplete() {
            return status == Status.COMPLETE;
        }

        StoredResponse toStoredResponse() {
            return new StoredResponse(statusCode, headers, body, completedAt);
        }
    }
}
