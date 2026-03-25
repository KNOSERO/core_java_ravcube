package com.ravcube.lib.event.test;

import com.ravcube.lib.event.config.TestApplication;
import com.ravcube.lib.event.domain.KafkaDomainEvent;
import com.ravcube.lib.event.inteface.EventPublisher;
import com.ravcube.lib.event.listener.KafkaCommitListener;
import com.ravcube.lib.event.listener.KafkaRollbackListener;
import com.ravcube.lib.event.publisher.DefaultKafkaRollbackPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ActiveProfiles({"test", "kafka"})
@SpringBootTest(classes = TestApplication.class)
class KafkaRollbackPublisherTest {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private EventPublisher publisher;

    @BeforeEach
    void beforeEach() {
        KafkaCommitListener.reset();
        KafkaRollbackListener.reset();
    }

    @Test
    void shouldPublishAndConsumeKafkaEventAfterRollback() {
        KafkaDomainEvent event = new KafkaDomainEvent(UUID.randomUUID(), "rollback");

        transactionTemplate.executeWithoutResult(status -> {
            publisher.publish(event);
            status.setRollbackOnly();
        });

        KafkaDomainEvent consumedEvent = KafkaRollbackListener.awaitEvent(Duration.ofSeconds(10));
        assertNotNull(consumedEvent);
        assertEquals(event.id(), consumedEvent.id());
        assertEquals(event.message(), consumedEvent.message());
        assertNull(KafkaCommitListener.awaitEvent(Duration.ofMillis(500)));
    }
}
