package com.ravcube.lib.stream.event;

import com.ravcube.lib.event.listener.DefaultNatsCommitListener;
import com.ravcube.lib.stream.application.ClientStreamService;
import com.ravcube.lib.stream.common.event.ClientStreamRefreshEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Profile("nats")
final class ClientStreamRefreshNatsListener extends DefaultNatsCommitListener<ClientStreamRefreshEvent> {

    private final ClientStreamService service;

    ClientStreamRefreshNatsListener(ClientStreamService service) {
        this.service = Objects.requireNonNull(service, "service must not be null");
    }

    @Override
    public void on(ClientStreamRefreshEvent event) {
        service.refresh(event.resourceName(), event.resourceId());
    }
}
