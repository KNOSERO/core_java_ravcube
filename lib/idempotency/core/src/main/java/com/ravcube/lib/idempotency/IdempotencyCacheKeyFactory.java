package com.ravcube.lib.idempotency;

import java.util.Objects;

final class IdempotencyCacheKeyFactory {

    private static final String DEFAULT_PREFIX = "idempotency";

    private final String prefix;

    IdempotencyCacheKeyFactory(String prefix) {
        this.prefix = normalizePrefix(prefix);
    }

    IdempotencyCacheKey from(String key) {
        String normalizedKey = Objects.requireNonNull(key, "key must not be null").trim();
        if (normalizedKey.isEmpty()) {
            throw new IllegalArgumentException("key must not be blank");
        }
        return new IdempotencyCacheKey(prefix + normalizedKey);
    }

    private String normalizePrefix(String prefix) {
        String normalizedPrefix = Objects.requireNonNullElse(prefix, DEFAULT_PREFIX).trim();
        if (normalizedPrefix.isEmpty()) {
            normalizedPrefix = DEFAULT_PREFIX;
        }
        return normalizedPrefix.endsWith(":") ? normalizedPrefix : normalizedPrefix + ":";
    }
}
