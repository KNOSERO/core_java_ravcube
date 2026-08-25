package com.ravcube.lib.stream.event;

import com.ravcube.lib.event.publisher.DefaultKafkaPublisher;
import com.ravcube.lib.stream.common.event.ClientStreamRefreshEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Profile("kafka")
final class ClientStreamRefreshPublisher extends DefaultKafkaPublisher<ClientStreamRefreshEvent> {

    private final ClientStreamKafkaProperties properties;

    ClientStreamRefreshPublisher(ClientStreamKafkaProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
    }

    @Override
    protected String baseTopic(ClientStreamRefreshEvent event) {
        return properties.topic();
    }
}
