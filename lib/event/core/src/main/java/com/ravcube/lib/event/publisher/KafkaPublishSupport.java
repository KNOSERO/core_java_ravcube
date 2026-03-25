package com.ravcube.lib.event.publisher;

import com.ravcube.lib.event.DomainEvent;
import com.ravcube.lib.event.enums.EventSource;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

final class KafkaPublishSupport<E extends DomainEvent> {

    private final KafkaTemplate<String, E> kafkaTemplate;
    private final EventSource eventSource;
    private final KafkaPublisherHeaders headers;

    KafkaPublishSupport(KafkaTemplate<String, E> kafkaTemplate, EventSource source) {
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate, "kafkaTemplate must not be null");
        this.eventSource = Objects.requireNonNull(source, "source must not be null");
        this.headers = new KafkaPublisherHeaders(eventSource.name().getBytes(StandardCharsets.UTF_8));
    }

    void publish(E event) {
        final E payload = Objects.requireNonNull(event, "payload must not be null");

        final String baseTopic = DomainEvent.getTopic(payload.getClass());
        final String topic = eventSource.formatTopic(baseTopic);
        final String key = Objects.requireNonNull(payload.getKey(), "key must not be null");

        final ProducerRecord<String, E> record = new ProducerRecord<>(topic, key, payload);
        headers.applyTo(record);

        kafkaTemplate.send(record);
    }
}
