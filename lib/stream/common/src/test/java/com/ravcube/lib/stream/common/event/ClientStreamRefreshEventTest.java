package com.ravcube.lib.stream.common.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientStreamRefreshEventTest {

    @Test
    void eventContainsVersionAndUsesResourceIdentityAsKey() {
        final ClientStreamRefreshEvent event =
                new ClientStreamRefreshEvent("claims", "1", 42);

        assertEquals("claims:1", event.getKey());
        assertEquals(42, event.version());
    }
}
