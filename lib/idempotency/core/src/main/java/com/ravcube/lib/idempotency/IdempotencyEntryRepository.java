package com.ravcube.lib.idempotency;

import com.ravcube.lib.cache.CacheStore;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

final class IdempotencyEntryRepository {

    private final CacheStore cacheStore;

    IdempotencyEntryRepository(CacheStore cacheStore) {
        this.cacheStore = Objects.requireNonNull(cacheStore, "cacheStore must not be null");
    }

    Optional<IdempotencyEntry> find(IdempotencyCacheKey cacheKey) {
        return cacheStore.get(cacheKey.value(), IdempotencyEntry.class);
    }

    boolean putIfAbsent(IdempotencyCacheKey cacheKey, IdempotencyEntry entry, Duration ttl) {
        return cacheStore.putIfAbsent(cacheKey.value(), entry, ttl);
    }

    boolean replace(IdempotencyCacheKey cacheKey, IdempotencyEntry expectedEntry, IdempotencyEntry newEntry, Duration ttl) {
        return cacheStore.replace(cacheKey.value(), expectedEntry, newEntry, ttl);
    }
}
