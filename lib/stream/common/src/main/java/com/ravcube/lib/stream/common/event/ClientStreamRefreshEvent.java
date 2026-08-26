package com.ravcube.lib.stream.common.event;

import com.ravcube.lib.event.DomainEvent;
import com.ravcube.lib.event.annotation.Topic;

import java.util.Objects;

@Topic("stream.resource.refresh")
public record ClientStreamRefreshEvent(
        String resourceName,
        String resourceId,
        long version
) implements DomainEvent {

    public ClientStreamRefreshEvent {
        resourceName = requireText(resourceName, "resourceName");
        resourceId = requireText(resourceId, "resourceId");
        if (version < 0) {
            throw new IllegalArgumentException("version must not be negative");
        }
    }

    @Override
    public String getKey() {
        return resourceName + ":" + resourceId;
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
