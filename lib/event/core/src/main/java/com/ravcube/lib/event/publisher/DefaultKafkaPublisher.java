package com.ravcube.lib.event.publisher;

import com.ravcube.lib.event.DomainEvent;
import com.ravcube.lib.event.enums.EventSource;
import com.ravcube.lib.event.kafka.KafkaPublishSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Objects;

public class DefaultKafkaPublisher<E extends DomainEvent> extends AbstractCommitPublisher<E> {

    private KafkaPublishSupport<E> kafkaPublisher;

    @Override
    protected void on(E event) {
        Objects.requireNonNull(kafkaPublisher, "kafkaPublisher must not be null").publish(event);
    }

    @Override
    public EventSource source() {
        return EventSource.KAFKA_AFTER_COMMIT;
    }

    @Autowired
    private void setKafkaTemplate(KafkaTemplate<String, E> kafkaTemplate) {
        this.kafkaPublisher = new KafkaPublishSupport<>(kafkaTemplate, source());
    }
}
