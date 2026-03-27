package com.ravcube.lib.cache;

import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

public class RedisCacheStore implements CacheStore {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheStore(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate must not be null");
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> valueType) {
        final String cacheKey = Objects.requireNonNull(key, "key must not be null");
        final Class<T> type = Objects.requireNonNull(valueType, "valueType must not be null");
        final Object value = redisTemplate.opsForValue().get(cacheKey);
        if (value == null) {
            return Optional.empty();
        }
        if (!type.isInstance(value)) {
            throw new IllegalStateException("Cached value for key '" + cacheKey + "' is not of type " + type.getName());
        }
        return Optional.of(type.cast(value));
    }

    @Override
    public <T> void put(String key, T value) {
        final String cacheKey = Objects.requireNonNull(key, "key must not be null");
        final T cacheValue = Objects.requireNonNull(value, "value must not be null");
        redisTemplate.opsForValue().set(cacheKey, cacheValue);
    }

    @Override
    public <T> void put(String key, T value, Duration ttl) {
        final String cacheKey = Objects.requireNonNull(key, "key must not be null");
        final T cacheValue = Objects.requireNonNull(value, "value must not be null");
        final Duration entryTtl = Objects.requireNonNull(ttl, "ttl must not be null");
        redisTemplate.opsForValue().set(cacheKey, cacheValue, entryTtl);
    }

    @Override
    public void delete(String key) {
        final String cacheKey = Objects.requireNonNull(key, "key must not be null");
        redisTemplate.delete(cacheKey);
    }
}
