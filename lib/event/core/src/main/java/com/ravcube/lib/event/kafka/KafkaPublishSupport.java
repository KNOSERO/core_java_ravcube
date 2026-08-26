package com.ravcube.lib.event.kafka;

import com.ravcube.lib.event.DomainEvent;
import com.ravcube.lib.event.enums.EventSource;
import com.ravcube.lib.logger.Logger;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class KafkaPublishSupport<E extends DomainEvent> {

    private final KafkaTemplate<String, E> kafkaTemplate;
    private final EventSource eventSource;
    private final KafkaPublisherHeaders headers;
    private final Logger logger;

    public KafkaPublishSupport(KafkaTemplate<String, E> kafkaTemplate, EventSource source) {
        this(kafkaTemplate, source, Logger.noop());
    }

    public KafkaPublishSupport(
            KafkaTemplate<String, E> kafkaTemplate,
            EventSource source,
            Logger logger
    ) {
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate, "kafkaTemplate must not be null");
        this.eventSource = Objects.requireNonNull(source, "source must not be null");
        this.headers = new KafkaPublisherHeaders(eventSource.name().getBytes(StandardCharsets.UTF_8));
        this.logger = Objects.requireNonNull(logger, "logger must not be null");
    }

    public void publish(E event) {
        publish(event, DomainEvent.getTopic(event.getClass()));
    }

    public void publish(E event, String baseTopic) {
        publish(event, baseTopic, 1);
    }

    public void publish(E event, String baseTopic, int maxAttempts) {
        publish(event, baseTopic, maxAttempts, Duration.ZERO, () -> {
        });
    }

    public void publish(
            E event,
            String baseTopic,
            int maxAttempts,
            Duration retryBackoff,
            Runnable onExhausted
    ) {
        final E payload = Objects.requireNonNull(event, "payload must not be null");
        final String validatedBaseTopic = requireText(baseTopic, "baseTopic");
        final Duration validatedBackoff = Objects.requireNonNull(
                retryBackoff,
                "retryBackoff must not be null"
        );
        if (validatedBackoff.isNegative()) {
            throw new IllegalArgumentException("retryBackoff must not be negative");
        }
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts must be greater than zero");
        }
        send(
                payload,
                validatedBaseTopic,
                maxAttempts,
                validatedBackoff,
                Objects.requireNonNull(onExhausted, "onExhausted must not be null")
        );
    }

    private void send(
            E payload,
            String baseTopic,
            int attemptsRemaining,
            Duration retryBackoff,
            Runnable onExhausted
    ) {
        final String topic = eventSource.formatTopic(baseTopic);
        final String key = Objects.requireNonNull(payload.getKey(), "key must not be null");
        final ProducerRecord<String, E> record = new ProducerRecord<>(topic, key, payload);
        headers.applyTo(record);

        kafkaTemplate.send(record).whenComplete((result, failure) -> {
            if (failure != null && attemptsRemaining > 1) {
                logger.warn(
                        "Kafka publish failed for topic {} and key {}; retrying",
                        topic,
                        key
                );
                CompletableFuture.delayedExecutor(
                        retryBackoff.toMillis(),
                        TimeUnit.MILLISECONDS
                ).execute(() -> send(
                        payload,
                        baseTopic,
                        attemptsRemaining - 1,
                        retryBackoff,
                        onExhausted
                ));
                return;
            }
            if (failure != null) {
                try {
                    onExhausted.run();
                } catch (RuntimeException callbackFailure) {
                    logger.warn(
                            "Kafka publish failure callback failed for topic {} and key {}",
                            topic,
                            key
                    );
                }
                logger.error(
                        "Kafka publish failed for topic {} and key {}",
                        failure,
                        topic,
                        key
                );
                return;
            }
            logger.debug("Kafka event accepted for topic {} and key {}", topic, key);
        });
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
