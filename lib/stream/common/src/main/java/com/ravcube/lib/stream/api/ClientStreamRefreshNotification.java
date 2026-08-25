package com.ravcube.lib.stream.api;

import java.util.Objects;

public record ClientStreamRefreshNotification(
        String resourceName,
        String resourceId
) {

    public ClientStreamRefreshNotification {
        resourceName = requireText(resourceName, "resourceName");
        resourceId = requireText(resourceId, "resourceId");
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
