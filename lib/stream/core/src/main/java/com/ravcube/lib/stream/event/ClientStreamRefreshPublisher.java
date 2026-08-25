package com.ravcube.lib.stream.event;

import com.ravcube.lib.event.publisher.DefaultCommitPublisher;
import com.ravcube.lib.stream.common.event.ClientStreamRefreshEvent;
import org.springframework.stereotype.Component;

@Component
final class ClientStreamRefreshPublisher extends DefaultCommitPublisher<ClientStreamRefreshEvent> {
}
