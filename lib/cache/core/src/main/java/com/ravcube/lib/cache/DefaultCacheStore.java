package com.ravcube.lib.cache;

import java.time.Duration;
import java.util.Optional;

public class DefaultCacheStore implements CacheStore {

    @Override
    public <T> Optional<T> get(String key, Class<T> valueType) {
        return Optional.empty();
    }

    @Override
    public <T> void put(String key, T value) {

    }

    @Override
    public <T> void put(String key, T value, Duration ttl) {

    }

    @Override
    public void delete(String key) {

    }
}
