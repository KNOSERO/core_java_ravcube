package com.ravcube.lib.stream.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientStreamRefreshNotificationTest {

    @Test
    void notificationContainsChangedResourceVersion() {
        final ClientStreamRefreshNotification notification =
                new ClientStreamRefreshNotification("1", 42);

        assertEquals("1", notification.resourceId());
        assertEquals(42, notification.version());
    }
}
