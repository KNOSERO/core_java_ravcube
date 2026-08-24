package com.ravcube.lib.stream;

import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public final class DefaultClientStreamPublisher implements ClientStreamPublisher {

    private final ClientStreamRegistry registry;

    public DefaultClientStreamPublisher(ClientStreamRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
    }

    @Override
    public void publish(String resourceName, String resourceId, Object payload) {
        registry.publish(resourceName, resourceId, payload);
    }
}
