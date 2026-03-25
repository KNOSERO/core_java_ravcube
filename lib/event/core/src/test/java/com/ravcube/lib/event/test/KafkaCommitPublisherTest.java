package com.ravcube.lib.event.test;

import com.ravcube.lib.event.config.TestApplication;
import com.ravcube.lib.event.domain.KafkaDomainEvent;
import com.ravcube.lib.event.inteface.EventPublisher;
import com.ravcube.lib.event.listener.KafkaCommitListener;
import com.ravcube.lib.event.listener.KafkaRollbackListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ActiveProfiles({"test", "kafka"})
@SpringBootTest(classes = TestApplication.class)
class KafkaCommitPublisherTest {

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
    void shouldPublishAndConsumeKafkaEventAfterCommit() {
        KafkaDomainEvent event = new KafkaDomainEvent(UUID.randomUUID(), "commit");

        transactionTemplate.executeWithoutResult(status -> publisher.publish(event));

        KafkaDomainEvent consumedEvent = KafkaCommitListener.awaitEvent(Duration.ofSeconds(10));
        assertNotNull(consumedEvent);
        assertEquals(event.id(), consumedEvent.id());
        assertEquals(event.message(), consumedEvent.message());
        assertNull(KafkaRollbackListener.awaitEvent(Duration.ofMillis(500)));
    }
}
