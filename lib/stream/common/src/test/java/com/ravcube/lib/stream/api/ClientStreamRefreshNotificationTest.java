package com.ravcube.lib.stream.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientStreamRefreshNotificationTest {

    @Test
    void notificationContainsChangedResourceIdentity() {
        final ClientStreamRefreshNotification notification =
                new ClientStreamRefreshNotification("claims", "1");

        assertEquals("claims", notification.resourceName());
        assertEquals("1", notification.resourceId());
    }
}
