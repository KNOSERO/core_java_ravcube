package com.ravcube.lib.stream.event;

import com.ravcube.lib.event.publisher.DefaultKafkaPublisher;
import com.ravcube.lib.stream.common.event.ClientStreamRefreshEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Objects;

@Component
@Profile("kafka")
final class ClientStreamRefreshPublisher extends DefaultKafkaPublisher<ClientStreamRefreshEvent> {

    private static final int MAX_PUBLISH_ATTEMPTS = 3;

    private final ClientStreamKafkaProperties properties;

    ClientStreamRefreshPublisher(ClientStreamKafkaProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
    }

    @EventListener
    @Order(Ordered.HIGHEST_PRECEDENCE)
    void requireTransaction(ClientStreamRefreshEvent event) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException(
                    "ClientStreamRefreshEvent must be published inside an active transaction"
            );
        }
    }

    @Override
    protected void on(ClientStreamRefreshEvent event) {
        publishToKafka(event, baseTopic(event), MAX_PUBLISH_ATTEMPTS);
    }

    @Override
    protected String baseTopic(ClientStreamRefreshEvent event) {
        return properties.topic();
    }
}
