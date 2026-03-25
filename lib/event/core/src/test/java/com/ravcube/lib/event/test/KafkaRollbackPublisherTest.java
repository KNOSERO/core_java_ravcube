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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ActiveProfiles({"test", "kafka"})
@SpringBootTest(
        classes = TestApplication.class,
        properties = {
                "spring.kafka.consumer.group-id=event-core-kafka-rollback-test"
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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

        KafkaDomainEvent consumedEvent = KafkaRollbackListener.awaitEventMatching(event.id(), Duration.ofSeconds(10));
        KafkaDomainEvent duplicateEvent = KafkaRollbackListener.awaitEventMatching(event.id(), Duration.ofMillis(500));
        assertNotNull(consumedEvent);
        assertNull(duplicateEvent);
        assertEquals(event.id(), consumedEvent.id());
        assertEquals(event.message(), consumedEvent.message());
        assertEquals(1, KafkaRollbackListener.invocations(event.id()));
        assertEquals(0, KafkaCommitListener.invocations(event.id()));
        assertNull(KafkaCommitListener.awaitEventMatching(event.id(), Duration.ofMillis(500)));
    }
}
