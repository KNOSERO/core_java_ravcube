package com.ravcube.lib.idempotency;

import com.ravcube.lib.cache.DefaultCacheStore;
import io.github.josipmusa.idempotency.core.AcquireResult;
import io.github.josipmusa.idempotency.core.IdempotencyContext;
import io.github.josipmusa.idempotency.core.StoredResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

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
    void shouldNotCompleteEntryAfterExpiredLockWasStolen() throws InterruptedException {
        CacheStoreIdempotencyStore slowStore = new CacheStoreIdempotencyStore(cacheStore, "idempotency");
        CacheStoreIdempotencyStore retryStore = new CacheStoreIdempotencyStore(cacheStore, "idempotency");
        IdempotencyContext context = context("stolen-key", Duration.ofMillis(2));

        slowStore.tryAcquire(context);
        Thread.sleep(10);
        retryStore.tryAcquire(context);

        StoredResponse lateResponse = new StoredResponse(200, Map.of(), new byte[0], Instant.now());
        assertThrows(
                IllegalStateException.class,
                () -> slowStore.complete(context.key(), lateResponse, Duration.ofMinutes(5))
        );
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
