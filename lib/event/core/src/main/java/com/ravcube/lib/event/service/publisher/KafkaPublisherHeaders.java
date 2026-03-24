package com.ravcube.lib.event.service.publisher;

import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

final class KafkaPublisherHeaders {

    private static final String SOURCE_HEADER = "source";
    private final byte[] sourceHeaderValue;

    KafkaPublisherHeaders(byte[] sourceHeaderValue) {
        this.sourceHeaderValue = Arrays.copyOf(
                Objects.requireNonNull(sourceHeaderValue, "sourceHeaderValue must not be null"),
                sourceHeaderValue.length
        );
    }

    <E> void applyTo(ProducerRecord<String, E> record) {
        applyTo(record, Map.of());
    }

    <E> void applyTo(ProducerRecord<String, E> record, Map<String, byte[]> additionalHeaders) {
        final ProducerRecord<String, E> target = Objects.requireNonNull(record, "record must not be null");
        addHeader(target, SOURCE_HEADER, sourceHeaderValue);

        Objects.requireNonNull(additionalHeaders, "additionalHeaders must not be null")
                .forEach((headerName, headerValue) -> addHeader(target, headerName, headerValue));
    }

    private <E> void addHeader(ProducerRecord<String, E> record, String headerName, byte[] headerValue) {
        final String name = Objects.requireNonNull(headerName, "headerName must not be null");
        final byte[] value = Arrays.copyOf(
                Objects.requireNonNull(headerValue, "headerValue must not be null"),
                headerValue.length
        );

        record.headers().add(name, value);
    }
}
