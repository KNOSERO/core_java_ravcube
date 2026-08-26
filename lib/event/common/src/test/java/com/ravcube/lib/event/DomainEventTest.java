package com.ravcube.lib.event;

import com.ravcube.lib.event.annotation.Topic;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DomainEventTest {

    @Test
    void topicIsReadFromTheEventType() {
        assertEquals("policy.created", DomainEvent.getTopic(PolicyCreated.class));
    }

    @Test
    void eventWithoutTopicIsRejectedWhenTopicIsRequired() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DomainEvent.getTopic(UnroutedEvent.class)
        );
    }

    @Topic("policy.created")
    private record PolicyCreated(String policyId) implements DomainEvent {
    }

    private record UnroutedEvent(String policyId) implements DomainEvent {
    }
}
