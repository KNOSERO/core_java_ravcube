package com.ravcube.lib.stream.application;

import com.ravcube.lib.stream.infrastructure.sse.ClientStreamRegistry;
import com.ravcube.lib.stream.infrastructure.sse.ClientStreamRegistryTestSupport;
import org.junit.jupiter.api.Test;

import static com.ravcube.lib.stream.infrastructure.sse.ClientStreamRegistryTestSupport.RecordingSseEmitter;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientStreamServiceTest {

    @Test
    void changedResourceNotifiesSubscribedClient() {
        final RecordingSseEmitter emitter = new RecordingSseEmitter();
        final ClientStreamRegistry registry = ClientStreamRegistryTestSupport.registry(emitter);
        final ClientStreamService service = new ClientStreamService(registry);

        service.subscribe("claims", "1");
        service.refresh("claims", "1", 42);

        assertEquals(1, emitter.eventCount());
    }
}
