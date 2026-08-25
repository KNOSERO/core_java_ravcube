package com.ravcube.lib.stream.infrastructure.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientStreamPropertiesTest {

    @Test
    void defaultsMatchStreamProfile() {
        final ClientStreamProperties properties = new ClientStreamProperties();

        assertEquals(Duration.ofMinutes(30), properties.timeout());
        assertEquals(100, properties.maxIdsPerSubscription());
        assertEquals(1_000, properties.maxSubscriptions());
        assertEquals(100, properties.maxSubscriptionsPerClient());
        assertEquals(100, properties.maxPendingEventsPerSubscription());
        assertEquals(Duration.ofSeconds(15), properties.heartbeatInterval());
    }

    @Test
    void configuredValuesAreKept() {
        final ClientStreamProperties properties = new ClientStreamProperties(
                Duration.ofMinutes(10),
                10,
                20,
                5,
                30,
                Duration.ofSeconds(20)
        );

        assertEquals(Duration.ofMinutes(10), properties.timeout());
        assertEquals(10, properties.maxIdsPerSubscription());
        assertEquals(20, properties.maxSubscriptions());
        assertEquals(5, properties.maxSubscriptionsPerClient());
        assertEquals(30, properties.maxPendingEventsPerSubscription());
        assertEquals(Duration.ofSeconds(20), properties.heartbeatInterval());
    }
}
