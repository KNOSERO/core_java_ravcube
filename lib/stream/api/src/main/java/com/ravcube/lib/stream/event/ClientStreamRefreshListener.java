package com.ravcube.lib.stream.event;

import com.ravcube.lib.stream.application.ClientStreamService;
import com.ravcube.lib.stream.common.event.ClientStreamRefreshEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Profile("kafka")
final class ClientStreamRefreshListener {

    private final ClientStreamService service;
    private final ClientStreamKafkaProperties properties;

    ClientStreamRefreshListener(
            ClientStreamService service,
            ClientStreamKafkaProperties properties
    ) {
        this.service = Objects.requireNonNull(service, "service must not be null");
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
    }

    @KafkaListener(
            topics = "#{__listener.commitTopic()}",
            groupId = "#{__listener.consumerGroup()}"
    )
    public void on(ClientStreamRefreshEvent event) {
        service.refresh(event.resourceName(), event.resourceId(), event.version());
    }

    public String commitTopic() {
        return properties.commitTopic();
    }

    public String consumerGroup() {
        return properties.consumerGroup();
    }
}
