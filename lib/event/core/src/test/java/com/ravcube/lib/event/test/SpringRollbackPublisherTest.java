package com.ravcube.lib.event.test;

import com.ravcube.lib.event.inteface.EventPublisher;
import com.ravcube.lib.event.config.TestApplication;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest(classes = TestApplication.class)
class SpringRollbackPublisherTest {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private EventPublisher publisher;

    @BeforeEach
    void beforeEach() {
        SpringCommitListener.reset();
        SpringRollbackListener.reset();
    }

    @Test
    void shouldHandleEventAfterRollback() {
        SpringDomainEvent event = new SpringDomainEvent(UUID.randomUUID(), "rollback");

        transactionTemplate.executeWithoutResult(status -> {
            publisher.publish(event);
            status.setRollbackOnly();
        });

        assertTrue(SpringRollbackListener.success());
        assertEquals(1, SpringRollbackListener.invocations());
        assertSame(event, SpringRollbackListener.lastEvent());
    }

    @Test
    void shouldNotHandleEventWhenTransactionCommits() {
        SpringDomainEvent event = new SpringDomainEvent(UUID.randomUUID(), "commit");

        transactionTemplate.executeWithoutResult(status -> publisher.publish(event));

        assertFalse(SpringRollbackListener.success());
        assertEquals(0, SpringRollbackListener.invocations());
        assertNull(SpringRollbackListener.lastEvent());
    }

    @Test
    void shouldNotHandleAnyEventBeforeTransactionCompletes() {
        SpringDomainEvent event = new SpringDomainEvent(UUID.randomUUID(), "in-progress");

        transactionTemplate.executeWithoutResult(status -> {
            publisher.publish(event);

            assertFalse(SpringRollbackListener.success());
            assertEquals(0, SpringRollbackListener.invocations());
            assertNull(SpringRollbackListener.lastEvent());

            assertFalse(SpringCommitListener.success());
            assertEquals(0, SpringCommitListener.invocations());
            assertNull(SpringCommitListener.lastEvent());
        });
    }
}
