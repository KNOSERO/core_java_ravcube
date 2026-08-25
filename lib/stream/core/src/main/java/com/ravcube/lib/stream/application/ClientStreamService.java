package com.ravcube.lib.stream.application;

import com.ravcube.lib.stream.infrastructure.sse.ClientStreamRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Component
public final class ClientStreamService {

    private final ClientStreamRegistry registry;

    public ClientStreamService(ClientStreamRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
    }

    public SseEmitter subscribe(
            String resourceName,
            Collection<String> resourceIds,
            String clientKey
    ) {
        return registry.subscribe(resourceName, resourceIds, clientKey);
    }

    public SseEmitter subscribe(
            String resourceName,
            String resourceId,
            String clientKey
    ) {
        return subscribe(resourceName, List.of(resourceId), clientKey);
    }

    public void refresh(String resourceName, String resourceId, long version) {
        registry.publish(resourceName, resourceId, version);
    }
}
