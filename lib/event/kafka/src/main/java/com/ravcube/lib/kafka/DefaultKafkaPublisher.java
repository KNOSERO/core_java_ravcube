package com.ravcube.lib.kafka;

import com.ravcube.lib.event.inteface.KafkaEvent;
import com.ravcube.lib.event.inteface.KafkaPublisher;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DefaultKafkaPublisher<T extends KafkaEvent> implements KafkaPublisher<T> {

    private final KafkaTemplate<String, T> kafkaTemplate;

    public DefaultKafkaPublisher(KafkaTemplate<String, T> kafkaTemplate) {
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate, "kafkaTemplate must not be null");
    }

    @Override
    public void publish(T evant) {
        publish(evant, new HashMap<>());
    }

    @Override
    public void publish(T evant, Map<String, byte[]> headers) {
        Objects.requireNonNull(evant, "payload must not be null");
        Objects.requireNonNull(evant.getTopic(), "topic must not be null");
        Objects.requireNonNull(evant.getKey(), "topic must not be null");

        final ProducerRecord<String, T> record = new ProducerRecord<>(evant.getTopic(), evant.getKey(), evant);
        headers.forEach((String key, byte[] values) -> record.headers().add(key, values));

        kafkaTemplate.send(record);
    }
}
