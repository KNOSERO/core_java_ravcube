package com.ravcube.lib.stream.api;

import java.util.Objects;

public record ClientStreamRefreshNotification(
        String resourceId,
        long version
) {

    public ClientStreamRefreshNotification {
        resourceId = requireText(resourceId, "resourceId");
        if (version < 0) {
            throw new IllegalArgumentException("version must not be negative");
        }
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
