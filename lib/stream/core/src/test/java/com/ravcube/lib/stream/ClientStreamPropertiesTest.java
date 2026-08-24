package com.ravcube.lib.stream;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientStreamPropertiesTest {

    @Test
    void shouldNormalizePathAndKeepConfiguredTimeout() {
        final ClientStreamProperties properties = new ClientStreamProperties(
                "/client-streams///",
                Duration.ofMinutes(10)
        );

        assertEquals("/client-streams", properties.path());
        assertEquals(Duration.ofMinutes(10), properties.timeout());
    }
}
