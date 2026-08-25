package com.ravcube.lib.stream.event;

import com.ravcube.lib.event.publisher.DefaultNatsPublisher;
import com.ravcube.lib.stream.common.event.ClientStreamRefreshEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("nats")
final class ClientStreamRefreshNatsPublisher extends DefaultNatsPublisher<ClientStreamRefreshEvent> {
}
