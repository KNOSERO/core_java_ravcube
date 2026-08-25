package com.ravcube.lib.stream.event;

import com.ravcube.lib.event.enums.EventSource;
import com.ravcube.lib.stream.application.ClientStreamService;
import com.ravcube.lib.stream.common.event.ClientStreamRefreshEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ClientStreamRefreshListenerTest {

    @Test
    void refreshEventRefreshesRequestedResource() {
        final ClientStreamService service = mock(ClientStreamService.class);
        final ClientStreamRefreshListener listener = new ClientStreamRefreshListener(service);

        listener.on(new ClientStreamRefreshEvent("claims", "1"));

        verify(service).refresh("claims", "1");
    }

    @Test
    void listenerUsesAfterCommitEvents() {
        final ClientStreamRefreshListener listener = new ClientStreamRefreshListener(
                mock(ClientStreamService.class)
        );

        assertEquals(EventSource.SPRING_AFTER_COMMIT, listener.source());
    }
}
