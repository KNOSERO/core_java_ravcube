package com.ravcube.lib.cache;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

import java.time.Duration;
import java.util.List;
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
    public <T> boolean putIfAbsent(String key, T value, Duration ttl) {
        final String cacheKey = Objects.requireNonNull(key, "key must not be null");
        final T cacheValue = Objects.requireNonNull(value, "value must not be null");
        final Duration entryTtl = Objects.requireNonNull(ttl, "ttl must not be null");
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(cacheKey, cacheValue, entryTtl));
    }

    @Override
    public <T> boolean replace(String key, T expectedValue, T newValue, Duration ttl) {
        final String cacheKey = Objects.requireNonNull(key, "key must not be null");
        final T expectedCacheValue = Objects.requireNonNull(expectedValue, "expectedValue must not be null");
        final T cacheValue = Objects.requireNonNull(newValue, "newValue must not be null");
        final Duration entryTtl = Objects.requireNonNull(ttl, "ttl must not be null");

        return Boolean.TRUE.equals(redisTemplate.execute(new SessionCallback<Boolean>() {
            @Override
            public Boolean execute(org.springframework.data.redis.core.RedisOperations operations) {
                operations.watch(cacheKey);
                Object currentValue = operations.opsForValue().get(cacheKey);
                if (!expectedCacheValue.equals(currentValue)) {
                    operations.unwatch();
                    return false;
                }

                operations.multi();
                operations.opsForValue().set(cacheKey, cacheValue, entryTtl);
                List<Object> results = operations.exec();
                return results != null;
            }
        }));
    }

    @Override
    public void delete(String key) {
        final String cacheKey = Objects.requireNonNull(key, "key must not be null");
        redisTemplate.delete(cacheKey);
    }
}
