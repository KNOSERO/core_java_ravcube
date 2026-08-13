package com.ravcube.lib.cache;

import java.time.Duration;
import java.util.Optional;

public interface CacheStore {

    <T> Optional<T> get(String key, Class<T> valueType);

    <T> void put(String key, T value);

    <T> void put(String key, T value, Duration ttl);

    <T> boolean putIfAbsent(String key, T value, Duration ttl);

    <T> boolean replace(String key, T expectedValue, T newValue, Duration ttl);

    void delete(String key);
}
