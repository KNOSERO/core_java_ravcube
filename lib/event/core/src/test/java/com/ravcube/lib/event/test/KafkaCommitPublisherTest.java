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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles({"test", "kafka", "test-kafka"})
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

        assertEquals(1, KafkaCommitListener.invocations(event.id()));
        assertEquals(0, KafkaRollbackListener.invocations(event.id()));
    }

    @Test
    void shouldInvokeEachCommitListenerExactlyOnceAfterCommit() {
        KafkaDomainEvent event = new KafkaDomainEvent(UUID.randomUUID(), "commit-two-listeners");

        transactionTemplate.executeWithoutResult(status -> publisher.publish(event));

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

        assertEquals(0, KafkaCommitListener.invocations(event.id()));
    }

    @Test
    void shouldNotHandleAnyEventBeforeTransactionCompletes() {
        KafkaDomainEvent event = new KafkaDomainEvent(UUID.randomUUID(), "in-progress");

        transactionTemplate.executeWithoutResult(status -> {
            publisher.publish(event);

            assertEquals(0, KafkaCommitListener.invocations(event.id()));
            assertEquals(0, KafkaRollbackListener.invocations(event.id()));
        });
    }
}
