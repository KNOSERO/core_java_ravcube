package com.ravcube.lib.idempotency;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

final class AcquiredIdempotencyEntries {

    private final ThreadLocal<Map<IdempotencyCacheKey, IdempotencyEntry>> entries = ThreadLocal.withInitial(HashMap::new);

    void remember(IdempotencyCacheKey cacheKey, IdempotencyEntry entry) {
        entries.get().put(cacheKey, entry);
    }

    Optional<IdempotencyEntry> find(IdempotencyCacheKey cacheKey) {
        return Optional.ofNullable(entries.get().get(cacheKey));
    }

    IdempotencyEntry required(IdempotencyCacheKey cacheKey, String key) {
        return find(cacheKey).orElseThrow(
                () -> new IllegalStateException("Idempotency entry was not acquired by this request for key " + key)
        );
    }

    void forget(IdempotencyCacheKey cacheKey) {
        Map<IdempotencyCacheKey, IdempotencyEntry> currentEntries = entries.get();
        currentEntries.remove(cacheKey);
        if (currentEntries.isEmpty()) {
            entries.remove();
        }
    }
}
