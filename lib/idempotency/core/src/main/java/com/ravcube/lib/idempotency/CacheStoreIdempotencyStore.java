package com.ravcube.lib.idempotency;

import com.ravcube.lib.cache.CacheStore;
import io.github.josipmusa.idempotency.core.AcquireResult;
import io.github.josipmusa.idempotency.core.IdempotencyContext;
import io.github.josipmusa.idempotency.core.IdempotencyStore;
import io.github.josipmusa.idempotency.core.StoredResponse;
import java.time.Clock;
import java.time.Duration;
import java.util.Objects;

public class CacheStoreIdempotencyStore implements IdempotencyStore {

    private static final Duration FAILED_ENTRY_TTL = Duration.ofMinutes(1);
    private static final int MAX_ACQUIRE_ATTEMPTS = 16;

    private final Clock clock;
    private final IdempotencyCacheKeyFactory cacheKeyFactory;
    private final IdempotencyEntryRepository entries;
    private final AcquiredIdempotencyEntries acquiredEntries = new AcquiredIdempotencyEntries();

    public CacheStoreIdempotencyStore(CacheStore cacheStore, String keyPrefix) {
        this(cacheStore, Clock.systemUTC(), keyPrefix);
    }

    CacheStoreIdempotencyStore(CacheStore cacheStore, Clock clock, String keyPrefix) {
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.cacheKeyFactory = new IdempotencyCacheKeyFactory(keyPrefix);
        this.entries = new IdempotencyEntryRepository(cacheStore);
    }

    @Override
    public AcquireResult tryAcquire(IdempotencyContext context) {
        IdempotencyContext idempotencyContext = Objects.requireNonNull(context, "context must not be null");
        IdempotencyCacheKey cacheKey = cacheKeyFactory.from(idempotencyContext.key());
        IdempotencyEntry newLock = IdempotencyEntry.inProgress(idempotencyContext, clock.instant());

        for (int attempt = 0; attempt < MAX_ACQUIRE_ATTEMPTS; attempt++) {
            IdempotencyEntry existing = entries.find(cacheKey).orElse(null);

            if (existing == null && entries.putIfAbsent(cacheKey, newLock, idempotencyContext.ttl())) {
                return acquired(cacheKey, newLock);
            }

            if (existing == null) {
                continue;
            }

            if (existing.isComplete()) {
                return completed(existing, idempotencyContext);
            }

            if (existing.canBeAcquired(clock.instant())
                    && entries.replace(cacheKey, existing, newLock, idempotencyContext.ttl())) {
                return acquired(cacheKey, newLock);
            }

            if (!existing.canBeAcquired(clock.instant())) {
                return AcquireResult.lockTimeout(idempotencyContext.key());
            }
        }

        return AcquireResult.lockTimeout(idempotencyContext.key());
    }

    @Override
    public void complete(String key, StoredResponse response, Duration ttl) {
        IdempotencyCacheKey cacheKey = cacheKeyFactory.from(key);
        IdempotencyEntry acquiredEntry = acquiredEntries.required(cacheKey, key);
        IdempotencyEntry completedEntry = acquiredEntry.complete(
                Objects.requireNonNull(response, "response must not be null")
        );

        try {
            if (!entries.replace(cacheKey, acquiredEntry, completedEntry, Objects.requireNonNull(ttl, "ttl must not be null"))) {
                throw new IllegalStateException("Idempotency entry is no longer owned by this request for key " + key);
            }
        } finally {
            acquiredEntries.forget(cacheKey);
        }
    }

    @Override
    public void release(String key) {
        IdempotencyCacheKey cacheKey = cacheKeyFactory.from(key);
        IdempotencyEntry acquiredEntry = acquiredEntries.find(cacheKey).orElse(null);

        if (acquiredEntry != null) {
            entries.replace(cacheKey, acquiredEntry, acquiredEntry.failed(FAILED_ENTRY_TTL), FAILED_ENTRY_TTL);
            acquiredEntries.forget(cacheKey);
            return;
        }

        entries.find(cacheKey)
                .filter(IdempotencyEntry::isInProgress)
                .ifPresent(existing -> entries.replace(cacheKey, existing, existing.failed(FAILED_ENTRY_TTL), FAILED_ENTRY_TTL));
    }

    @Override
    public void extendLock(String key, Duration ttl) {
        IdempotencyCacheKey cacheKey = cacheKeyFactory.from(key);
        entries.find(cacheKey)
                .filter(IdempotencyEntry::isInProgress)
                .ifPresent(existing -> entries.replace(cacheKey, existing, existing.extendLock(clock.instant(), ttl), existing.ttl()));
    }

    @Override
    public int purgeExpired() {
        return 0;
    }

    private AcquireResult acquired(IdempotencyCacheKey cacheKey, IdempotencyEntry entry) {
        acquiredEntries.remember(cacheKey, entry);
        return AcquireResult.acquired();
    }

    private AcquireResult completed(IdempotencyEntry entry, IdempotencyContext context) {
        if (!entry.hasSameFingerprint(context)) {
            return AcquireResult.fingerprintMismatch(entry.requestFingerprint(), context.requestFingerprint());
        }
        return AcquireResult.duplicate(entry.toStoredResponse());
    }
}
