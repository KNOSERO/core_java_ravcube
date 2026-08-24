package com.ravcube.lib.stream;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;

@Component
public final class DefaultClientStreamPublisher implements ClientStreamPublisher {

    private final ClientStreamRegistry registry;

    public DefaultClientStreamPublisher(ClientStreamRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
    }

    @Override
    public <T> void refresh(String resourceName, String resourceId, T payload) {
        registry.publishResource(resourceName, resourceId, payload);
    }

    @Override
    public <T> void refresh(String resourceName, T payload) {
        registry.publishCollection(resourceName, payload);
    }

    @Override
    public <T> void refresh(String resourceName, Collection<String> resourceIds, T payload) {
        registry.publishSelectedCollection(resourceName, resourceIds, payload);
    }
}
