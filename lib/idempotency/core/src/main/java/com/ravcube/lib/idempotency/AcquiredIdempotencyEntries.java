package com.ravcube.lib.idempotency;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final class AcquiredIdempotencyEntries {

    private final ConcurrentMap<IdempotencyCacheKey, IdempotencyEntry> entries = new ConcurrentHashMap<>();

    void remember(IdempotencyCacheKey cacheKey, IdempotencyEntry entry) {
        entries.put(cacheKey, entry);
    }

    Optional<IdempotencyEntry> find(IdempotencyCacheKey cacheKey) {
        return Optional.ofNullable(entries.get(cacheKey));
    }

    IdempotencyEntry required(IdempotencyCacheKey cacheKey, String key) {
        return find(cacheKey).orElseThrow(
                () -> new IllegalStateException("Idempotency entry was not acquired by this request for key " + key)
        );
    }

    boolean replace(IdempotencyCacheKey cacheKey, IdempotencyEntry expectedEntry, IdempotencyEntry newEntry) {
        return entries.replace(cacheKey, expectedEntry, newEntry);
    }

    void forget(IdempotencyCacheKey cacheKey) {
        entries.remove(cacheKey);
    }
}
