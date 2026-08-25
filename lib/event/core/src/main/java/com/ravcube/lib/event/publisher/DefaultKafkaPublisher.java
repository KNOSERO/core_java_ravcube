package com.ravcube.lib.event.publisher;

import com.ravcube.lib.event.DomainEvent;
import com.ravcube.lib.event.enums.EventSource;
import com.ravcube.lib.event.kafka.KafkaPublishSupport;
import com.ravcube.lib.logger.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Objects;

public class DefaultKafkaPublisher<E extends DomainEvent> extends AbstractCommitPublisher<E> {

    private KafkaPublishSupport<E> kafkaPublisher;

    @Override
    protected void on(E event) {
        publishToKafka(event, baseTopic(event));
    }

    protected final void publishToKafka(E event, String baseTopic) {
        publishToKafka(event, baseTopic, 1);
    }

    protected final void publishToKafka(E event, String baseTopic, int maxAttempts) {
        kafkaPublisher().publish(event, baseTopic, maxAttempts);
    }

    protected final KafkaPublishSupport<E> kafkaPublisher() {
        return Objects.requireNonNull(kafkaPublisher, "kafkaPublisher must not be null");
    }

    protected String baseTopic(E event) {
        return DomainEvent.getTopic(event.getClass());
    }

    @Override
    public EventSource source() {
        return EventSource.KAFKA_AFTER_COMMIT;
    }

    @Autowired
    private void setKafkaTemplate(
            KafkaTemplate<String, E> kafkaTemplate,
            LoggerFactory loggerFactory
    ) {
        this.kafkaPublisher = new KafkaPublishSupport<>(
                kafkaTemplate,
                source(),
                loggerFactory.getLogger(getClass())
        );
    }
}
