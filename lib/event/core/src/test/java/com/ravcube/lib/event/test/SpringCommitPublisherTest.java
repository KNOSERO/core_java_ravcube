package com.ravcube.lib.event.test;

import com.ravcube.lib.event.inteface.EventPublisher;
import com.ravcube.lib.event.config.TestApplication;
import com.ravcube.lib.event.listener.SpringCommitAuditListener;
import com.ravcube.lib.event.domain.SpringDomainEvent;
import com.ravcube.lib.event.listener.SpringCommitListener;
import com.ravcube.lib.event.listener.SpringRollbackListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;

@ActiveProfiles("test")
@SpringBootTest(classes = TestApplication.class)
class SpringCommitPublisherTest {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private EventPublisher publisher;

    @BeforeEach
    void beforeEach() {
        SpringCommitListener.reset();
        SpringCommitAuditListener.reset();
        SpringRollbackListener.reset();
    }

    @Test
    void shouldHandleEventAfterCommit() {
        SpringDomainEvent event = new SpringDomainEvent(UUID.randomUUID(), "commit");

        transactionTemplate.executeWithoutResult(status -> publisher.publish(event));

        assertTrue(SpringCommitListener.success());
        assertEquals(1, SpringCommitListener.invocations());
        assertSame(event, SpringCommitListener.lastEvent());
    }

    @Test
    void shouldInvokeEachCommitListenerExactlyOnceAfterCommit() {
        SpringDomainEvent event = new SpringDomainEvent(UUID.randomUUID(), "commit-two-listeners");

        transactionTemplate.executeWithoutResult(status -> publisher.publish(event));

        assertEquals(1, SpringCommitListener.invocations());
        assertEquals(1, SpringCommitAuditListener.invocations());
    }

    @Test
    void shouldNotHandleEventWhenTransactionRollsBack() {
        SpringDomainEvent event = new SpringDomainEvent(UUID.randomUUID(), "rollback");

        transactionTemplate.executeWithoutResult(status -> {
            publisher.publish(event);
            status.setRollbackOnly();
        });

        assertFalse(SpringCommitListener.success());
        assertEquals(0, SpringCommitListener.invocations());
        assertNull(SpringCommitListener.lastEvent());
    }

    @Test
    void shouldNotHandleAnyEventBeforeTransactionCompletes() {
        SpringDomainEvent event = new SpringDomainEvent(UUID.randomUUID(), "in-progress");

        transactionTemplate.executeWithoutResult(status -> {
            publisher.publish(event);

            assertFalse(SpringCommitListener.success());
            assertEquals(0, SpringCommitListener.invocations());
            assertNull(SpringCommitListener.lastEvent());

            assertFalse(SpringRollbackListener.success());
            assertEquals(0, SpringRollbackListener.invocations());
            assertNull(SpringRollbackListener.lastEvent());
        });
    }
}
