package com.ravcube.lib.idempotency;

import com.ravcube.lib.cache.DefaultCacheStore;
import io.github.josipmusa.idempotency.core.AcquireResult;
import io.github.josipmusa.idempotency.core.IdempotencyContext;
import io.github.josipmusa.idempotency.core.StoredResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static com.ravcube.test.awaitility.Eventually.untilAsserted;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CacheStoreIdempotencyStoreTest {

    private static final String FINGERPRINT = "0123456789abcdef";

    private final DefaultCacheStore cacheStore = new DefaultCacheStore();

    @Test
    void shouldAcquireKeyOnlyOnceAcrossStoreInstancesSharingCache() {
        CacheStoreIdempotencyStore firstStore = new CacheStoreIdempotencyStore(cacheStore, "idempotency");
        CacheStoreIdempotencyStore secondStore = new CacheStoreIdempotencyStore(cacheStore, "idempotency");
        IdempotencyContext context = context("same-key", Duration.ofSeconds(10));

        AcquireResult firstResult = firstStore.tryAcquire(context);
        AcquireResult secondResult = secondStore.tryAcquire(context);

        assertInstanceOf(AcquireResult.Acquired.class, firstResult);
        assertInstanceOf(AcquireResult.LockTimeout.class, secondResult);
    }

    @Test
    void shouldNotCompleteEntryAfterExpiredLockWasStolen() {
        CacheStoreIdempotencyStore slowStore = new CacheStoreIdempotencyStore(cacheStore, "idempotency");
        CacheStoreIdempotencyStore retryStore = new CacheStoreIdempotencyStore(cacheStore, "idempotency");
        IdempotencyContext context = context("stolen-key", Duration.ofMillis(2));

        slowStore.tryAcquire(context);
        untilAsserted(
                Duration.ofSeconds(1),
                Duration.ofMillis(10),
                () -> assertInstanceOf(AcquireResult.Acquired.class, retryStore.tryAcquire(context))
        );

        StoredResponse lateResponse = new StoredResponse(200, Map.of(), new byte[0], Instant.now());
        assertThrows(
                IllegalStateException.class,
                () -> slowStore.complete(context.key(), lateResponse, Duration.ofMinutes(5))
        );
    }

    @Test
    void shouldCompleteEntryAfterLockWasExtendedByHeartbeatThread() throws InterruptedException {
        CacheStoreIdempotencyStore store = new CacheStoreIdempotencyStore(cacheStore, "idempotency");
        IdempotencyContext context = context("heartbeat-key", Duration.ofSeconds(10));
        StoredResponse response = new StoredResponse(200, Map.of(), new byte[0], Instant.now());

        store.tryAcquire(context);
        Thread heartbeatThread = new Thread(() -> store.extendLock(context.key(), Duration.ofSeconds(10)));
        heartbeatThread.start();
        heartbeatThread.join();

        assertDoesNotThrow(() -> store.complete(context.key(), response, Duration.ofMinutes(5)));
    }

    @Test
    void shouldNotExtendLockFromStoreInstanceThatDidNotAcquireKey() {
        CacheStoreIdempotencyStore ownerStore = new CacheStoreIdempotencyStore(cacheStore, "idempotency");
        CacheStoreIdempotencyStore outsiderStore = new CacheStoreIdempotencyStore(cacheStore, "idempotency");
        CacheStoreIdempotencyStore retryStore = new CacheStoreIdempotencyStore(cacheStore, "idempotency");
        IdempotencyContext context = context("outsider-heartbeat-key", Duration.ofMillis(20));

        ownerStore.tryAcquire(context);
        outsiderStore.extendLock(context.key(), Duration.ofSeconds(10));

        untilAsserted(
                Duration.ofSeconds(1),
                Duration.ofMillis(10),
                () -> assertInstanceOf(AcquireResult.Acquired.class, retryStore.tryAcquire(context))
        );
    }

    @Test
    void shouldNotReleaseLockFromStoreInstanceThatDidNotAcquireKey() {
        CacheStoreIdempotencyStore ownerStore = new CacheStoreIdempotencyStore(cacheStore, "idempotency");
        CacheStoreIdempotencyStore outsiderStore = new CacheStoreIdempotencyStore(cacheStore, "idempotency");
        CacheStoreIdempotencyStore retryStore = new CacheStoreIdempotencyStore(cacheStore, "idempotency");
        IdempotencyContext context = context("outsider-release-key", Duration.ofSeconds(10));

        ownerStore.tryAcquire(context);

        assertThrows(IllegalStateException.class, () -> outsiderStore.release(context.key()));
        assertInstanceOf(AcquireResult.LockTimeout.class, retryStore.tryAcquire(context));
    }

    @Test
    void shouldRejectBlankKey() {
        CacheStoreIdempotencyStore store = new CacheStoreIdempotencyStore(cacheStore, "idempotency");

        assertThrows(
                IllegalArgumentException.class,
                () -> store.complete("   ", new StoredResponse(200, Map.of(), new byte[0], Instant.now()), Duration.ofMinutes(5))
        );
    }

    private IdempotencyContext context(String key, Duration lockTimeout) {
        return new IdempotencyContext(key, Duration.ofMinutes(5), lockTimeout, FINGERPRINT);
    }
}
