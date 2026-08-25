package com.ravcube.lib.event.kafka;

import com.ravcube.lib.event.DomainEvent;
import com.ravcube.lib.event.enums.EventSource;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class KafkaPublishSupport<E extends DomainEvent> {

    private final KafkaTemplate<String, E> kafkaTemplate;
    private final EventSource eventSource;
    private final KafkaPublisherHeaders headers;

    public KafkaPublishSupport(KafkaTemplate<String, E> kafkaTemplate, EventSource source) {
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate, "kafkaTemplate must not be null");
        this.eventSource = Objects.requireNonNull(source, "source must not be null");
        this.headers = new KafkaPublisherHeaders(eventSource.name().getBytes(StandardCharsets.UTF_8));
    }

    public void publish(E event) {
        publish(event, DomainEvent.getTopic(event.getClass()));
    }

    public void publish(E event, String baseTopic) {
        final E payload = Objects.requireNonNull(event, "payload must not be null");
        final String validatedBaseTopic = requireText(baseTopic, "baseTopic");
        final String topic = eventSource.formatTopic(validatedBaseTopic);
        final String key = Objects.requireNonNull(payload.getKey(), "key must not be null");

        final ProducerRecord<String, E> record = new ProducerRecord<>(topic, key, payload);
        headers.applyTo(record);

        kafkaTemplate.send(record);
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
