package com.ravcube.lib.stream.infrastructure.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientStreamPropertiesTest {

    @Test
    void defaultsMatchStreamProfile() {
        final ClientStreamProperties properties = new ClientStreamProperties(Duration.ofMinutes(30));

        assertEquals(Duration.ofMinutes(30), properties.timeout());
        assertEquals(100, properties.maxIdsPerSubscription());
        assertEquals(1_000, properties.maxSubscriptions());
        assertEquals(100, properties.maxPendingEventsPerSubscription());
    }

    @Test
    void configuredTimeoutIsKept() {
        final ClientStreamProperties properties = new ClientStreamProperties(
                Duration.ofMinutes(10),
                10,
                20
        );

        assertEquals(Duration.ofMinutes(10), properties.timeout());
        assertEquals(10, properties.maxIdsPerSubscription());
        assertEquals(20, properties.maxSubscriptions());
    }
}
