package com.ravcube.lib.event.test;

import com.ravcube.lib.event.config.TestApplication;
import com.ravcube.lib.event.domain.KafkaDomainEvent;
import com.ravcube.lib.event.inteface.EventPublisher;
import com.ravcube.lib.event.listener.KafkaCommitAuditListener;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles({"test", "kafka"})
@SpringBootTest(
        classes = TestApplication.class,
        properties = {
                "spring.kafka.consumer.group-id=event-core-kafka-commit-test"
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class KafkaCommitPublisherTest {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private EventPublisher publisher;

    @BeforeEach
    void beforeEach() {
        KafkaCommitAuditListener.reset();
        KafkaCommitListener.reset();
        KafkaRollbackListener.reset();
    }

    @Test
    void shouldHandleEventAfterCommit() {
        KafkaDomainEvent event = new KafkaDomainEvent(UUID.randomUUID(), "commit");

        transactionTemplate.executeWithoutResult(status -> publisher.publish(event));

        KafkaDomainEvent consumedEvent = KafkaCommitListener.awaitEventMatching(event.id(), Duration.ofSeconds(10));
        KafkaDomainEvent duplicateEvent = KafkaCommitListener.awaitEventMatching(event.id(), Duration.ofMillis(500));
        assertTrue(KafkaCommitListener.success());
        assertEquals(1, KafkaCommitListener.invocations(event.id()));
        assertNotNull(consumedEvent);
        assertNull(duplicateEvent);
        assertEquals(event.id(), consumedEvent.id());
        assertEquals(event.message(), consumedEvent.message());
        assertEquals(0, KafkaRollbackListener.invocations(event.id()));
    }

    @Test
    void shouldInvokeEachCommitListenerExactlyOnceAfterCommit() {
        KafkaDomainEvent event = new KafkaDomainEvent(UUID.randomUUID(), "commit-two-listeners");

        transactionTemplate.executeWithoutResult(status -> publisher.publish(event));

        assertNotNull(KafkaCommitListener.awaitEventMatching(event.id(), Duration.ofSeconds(10)));
        assertNotNull(KafkaCommitAuditListener.awaitEventMatching(event.id(), Duration.ofSeconds(10)));
        assertNull(KafkaCommitListener.awaitEventMatching(event.id(), Duration.ofMillis(500)));
        assertNull(KafkaCommitAuditListener.awaitEventMatching(event.id(), Duration.ofMillis(500)));
        assertEquals(1, KafkaCommitListener.invocations(event.id()));
        assertEquals(1, KafkaCommitAuditListener.invocations(event.id()));
        assertEquals(0, KafkaRollbackListener.invocations(event.id()));
    }

    @Test
    void shouldNotHandleEventWhenTransactionRollsBack() {
        KafkaDomainEvent event = new KafkaDomainEvent(UUID.randomUUID(), "rollback");

        transactionTemplate.executeWithoutResult(status -> {
            publisher.publish(event);
            status.setRollbackOnly();
        });

        assertFalse(KafkaCommitListener.success());
        assertEquals(0, KafkaCommitListener.invocations(event.id()));
        assertNull(KafkaCommitListener.awaitEventMatching(event.id(), Duration.ofSeconds(1)));
    }

    @Test
    void shouldNotHandleAnyEventBeforeTransactionCompletes() {
        KafkaDomainEvent event = new KafkaDomainEvent(UUID.randomUUID(), "in-progress");

        transactionTemplate.executeWithoutResult(status -> {
            publisher.publish(event);

            assertEquals(0, KafkaCommitListener.invocations(event.id()));
            assertNull(KafkaCommitListener.awaitEventMatching(event.id(), Duration.ofMillis(200)));

            assertEquals(0, KafkaRollbackListener.invocations(event.id()));
            assertNull(KafkaRollbackListener.awaitEventMatching(event.id(), Duration.ofMillis(200)));
        });
    }
}
