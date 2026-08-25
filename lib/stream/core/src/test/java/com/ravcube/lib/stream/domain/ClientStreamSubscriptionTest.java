package com.ravcube.lib.stream.domain;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientStreamSubscriptionTest {

    @Test
    void shouldAcceptOnlyMatchingAuthorizedResourceId() {
        final ClientStreamSubscription subscription = new ClientStreamSubscription(
                "claims",
                Set.of("1", "2"),
                resourceId -> resourceId.equals("1")
        );

        assertTrue(subscription.accepts("claims", "1"));
        assertFalse(subscription.accepts("claims", "2"));
        assertFalse(subscription.accepts("policies", "1"));
    }
}
