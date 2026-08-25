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
    private final ClientStreamResourceCatalog resourceCatalog;

    public ClientStreamService(
            ClientStreamRegistry registry,
            ClientStreamResourceCatalog resourceCatalog
    ) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        this.resourceCatalog = Objects.requireNonNull(
                resourceCatalog,
                "resourceCatalog must not be null"
        );
    }

    public SseEmitter subscribe(String resourceName, Collection<String> resourceIds) {
        return registry.subscribe(resourceName, resourceIds);
    }

    public SseEmitter subscribe(String resourceName, String resourceId) {
        final SseEmitter emitter = registry.subscribe(resourceName, List.of(resourceId));
        try {
            resourceCatalog.find(resourceName)
                    .map(resourceReader -> resourceReader.resource(resourceId))
                    .filter(Objects::nonNull)
                    .ifPresent(payload -> registry.sendInitial(
                            emitter,
                            resourceName,
                            resourceId,
                            payload
                    ));
            return emitter;
        } catch (RuntimeException exception) {
            registry.unsubscribe(emitter);
            emitter.completeWithError(exception);
            throw exception;
        }
    }

    public void refresh(String resourceName, String resourceId) {
        resourceCatalog.find(resourceName)
                .map(resourceReader -> resourceReader.resource(resourceId))
                .filter(Objects::nonNull)
                .ifPresent(payload -> registry.publish(resourceName, resourceId, payload));
    }
}
