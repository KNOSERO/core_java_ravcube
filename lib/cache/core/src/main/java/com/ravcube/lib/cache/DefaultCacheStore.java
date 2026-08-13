package com.ravcube.lib.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class DefaultCacheStore implements CacheStore {

    private final Map<String, CacheEntry> entries = new HashMap<>();

    @Override
    public synchronized <T> Optional<T> get(String key, Class<T> valueType) {
        final String cacheKey = Objects.requireNonNull(key, "key must not be null");
        final Class<T> type = Objects.requireNonNull(valueType, "valueType must not be null");
        CacheEntry entry = entries.get(cacheKey);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.isExpired()) {
            entries.remove(cacheKey);
            return Optional.empty();
        }
        if (!type.isInstance(entry.value())) {
            throw new IllegalStateException("Cached value for key '" + cacheKey + "' is not of type " + type.getName());
        }
        return Optional.of(type.cast(entry.value()));
    }

    @Override
    public synchronized <T> void put(String key, T value) {
        entries.put(
                Objects.requireNonNull(key, "key must not be null"),
                new CacheEntry(Objects.requireNonNull(value, "value must not be null"), null)
        );
    }

    @Override
    public synchronized <T> void put(String key, T value, Duration ttl) {
        entries.put(
                Objects.requireNonNull(key, "key must not be null"),
                CacheEntry.expiresAfter(
                        Objects.requireNonNull(value, "value must not be null"),
                        Objects.requireNonNull(ttl, "ttl must not be null")
                )
        );
    }

    @Override
    public synchronized <T> boolean putIfAbsent(String key, T value, Duration ttl) {
        final String cacheKey = Objects.requireNonNull(key, "key must not be null");
        if (get(cacheKey, Object.class).isPresent()) {
            return false;
        }
        put(cacheKey, value, ttl);
        return true;
    }

    @Override
    public synchronized <T> boolean replace(String key, T expectedValue, T newValue, Duration ttl) {
        final String cacheKey = Objects.requireNonNull(key, "key must not be null");
        final T expectedCacheValue = Objects.requireNonNull(expectedValue, "expectedValue must not be null");
        Optional<Object> currentValue = get(cacheKey, Object.class);
        if (currentValue.isEmpty() || !expectedCacheValue.equals(currentValue.get())) {
            return false;
        }
        put(cacheKey, newValue, ttl);
        return true;
    }

    @Override
    public synchronized void delete(String key) {
        entries.remove(Objects.requireNonNull(key, "key must not be null"));
    }

    private record CacheEntry(Object value, Instant expiresAt) {

        static CacheEntry expiresAfter(Object value, Duration ttl) {
            return new CacheEntry(value, Instant.now().plus(ttl));
        }

        boolean isExpired() {
            return expiresAt != null && !expiresAt.isAfter(Instant.now());
        }
    }
}
