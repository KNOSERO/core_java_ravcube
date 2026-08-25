package com.ravcube.lib.event.test;

import com.ravcube.lib.event.config.TestApplication;
import com.ravcube.lib.event.domain.NatsDomainEvent;
import com.ravcube.lib.event.inteface.EventPublisher;
import com.ravcube.lib.event.listener.NatsCommitListenerPodOne;
import com.ravcube.lib.event.listener.NatsCommitListenerPodTwo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static com.ravcube.test.nats.NatsTestProfiles.TEST_NATS_PROFILE;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles({"nats", TEST_NATS_PROFILE})
@SpringBootTest(classes = TestApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class NatsCommitPublisherTest {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private EventPublisher publisher;

    @BeforeEach
    void beforeEach() {
        NatsCommitListenerPodOne.reset();
        NatsCommitListenerPodTwo.reset();
    }

    @Test
    void shouldBroadcastCommittedEventToEveryPodSubscription() {
        NatsDomainEvent event = new NatsDomainEvent(UUID.randomUUID(), "broadcast");

        transactionTemplate.executeWithoutResult(status -> publisher.publish(event));

        assertEquals(1, NatsCommitListenerPodOne.invocations(event.id()));
        assertEquals(1, NatsCommitListenerPodTwo.invocations(event.id()));
    }

    @Test
    void shouldNotBroadcastEventBeforeTransactionCompletes() {
        NatsDomainEvent event = new NatsDomainEvent(UUID.randomUUID(), "in-progress");

        transactionTemplate.executeWithoutResult(status -> {
            publisher.publish(event);

            assertEquals(0, NatsCommitListenerPodOne.invocations(event.id()));
            assertEquals(0, NatsCommitListenerPodTwo.invocations(event.id()));
        });
    }

    @Test
    void shouldNotBroadcastEventWhenTransactionRollsBack() {
        NatsDomainEvent event = new NatsDomainEvent(UUID.randomUUID(), "rollback");

        transactionTemplate.executeWithoutResult(status -> {
            publisher.publish(event);
            status.setRollbackOnly();
        });

        assertEquals(0, NatsCommitListenerPodOne.invocations(event.id()));
        assertEquals(0, NatsCommitListenerPodTwo.invocations(event.id()));
    }
}
