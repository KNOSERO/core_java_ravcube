package com.ravcube.lib.stream.event;

import com.ravcube.lib.event.listener.DefaultCommitListener;
import com.ravcube.lib.stream.application.ClientStreamService;
import com.ravcube.lib.stream.common.event.ClientStreamRefreshEvent;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
final class ClientStreamRefreshListener extends DefaultCommitListener<ClientStreamRefreshEvent> {

    private final ClientStreamService service;

    ClientStreamRefreshListener(ClientStreamService service) {
        this.service = Objects.requireNonNull(service, "service must not be null");
    }

    @Override
    public void on(ClientStreamRefreshEvent event) {
        service.refresh(event.resourceName(), event.resourceId(), event.version());
    }
}
