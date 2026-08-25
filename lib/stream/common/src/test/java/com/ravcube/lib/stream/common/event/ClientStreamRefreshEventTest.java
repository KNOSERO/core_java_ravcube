package com.ravcube.lib.stream.common.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientStreamRefreshEventTest {

    @Test
    void shouldBuildStableEventKey() {
        final ClientStreamRefreshEvent event = new ClientStreamRefreshEvent("claims", "1");

        assertEquals("claims:1", event.getKey());
    }
}
