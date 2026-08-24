package com.ravcube.lib.stream;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientStreamPropertiesTest {

    @Test
    void shouldKeepConfiguredTimeout() {
        final ClientStreamProperties properties = new ClientStreamProperties(
                Duration.ofMinutes(10)
        );

        assertEquals(Duration.ofMinutes(10), properties.timeout());
    }
}
