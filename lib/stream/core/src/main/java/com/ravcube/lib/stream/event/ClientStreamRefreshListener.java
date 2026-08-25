package com.ravcube.lib.stream.event;

import com.ravcube.lib.event.listener.DefaultCommitListener;
import com.ravcube.lib.stream.api.ClientStreamResourceReader;
import com.ravcube.lib.stream.application.ClientStreamResourceCatalog;
import com.ravcube.lib.stream.common.event.ClientStreamRefreshEvent;
import com.ravcube.lib.stream.infrastructure.sse.ClientStreamRegistry;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
final class ClientStreamRefreshListener extends DefaultCommitListener<ClientStreamRefreshEvent> {

    private final ClientStreamResourceCatalog resourceCatalog;
    private final ClientStreamRegistry registry;

    ClientStreamRefreshListener(
            ClientStreamResourceCatalog resourceCatalog,
            ClientStreamRegistry registry
    ) {
        this.resourceCatalog = Objects.requireNonNull(
                resourceCatalog,
                "resourceCatalog must not be null"
        );
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
    }

    @Override
    public void on(ClientStreamRefreshEvent event) {
        resourceCatalog.find(event.resourceName())
                .map(resourceReader -> read(resourceReader, event.resourceId()))
                .filter(Objects::nonNull)
                .ifPresent(payload -> registry.publish(
                        event.resourceName(),
                        event.resourceId(),
                        payload
                ));
    }

    private static Object read(
            ClientStreamResourceReader<?> resourceReader,
            String resourceId
    ) {
        return resourceReader.resource(resourceId);
    }
}
