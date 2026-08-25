package com.ravcube.lib.stream.application;

import com.ravcube.lib.stream.api.ClientStreamResourceReader;
import com.ravcube.lib.stream.infrastructure.sse.ClientStreamRegistry;
import com.ravcube.lib.stream.infrastructure.sse.ClientStreamRegistryTestSupport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.ravcube.lib.stream.infrastructure.sse.ClientStreamRegistryTestSupport.RecordingSseEmitter;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientStreamServiceTest {

    @Test
    void singleResourceSubscriptionReceivesInitialResource() {
        final RecordingSseEmitter emitter = new RecordingSseEmitter();
        final ClientStreamRegistry registry = ClientStreamRegistryTestSupport.registry(
                (resourceName, resourceId) -> true,
                emitter
        );
        final ClientStreamResourceCatalog catalog = new ClientStreamResourceCatalog(
                List.of(resource("claims"))
        );
        final ClientStreamService service = new ClientStreamService(registry, catalog);

        service.subscribe("claims", "1");

        assertEquals(1, emitter.eventCount());
    }

    private static ClientStreamResourceReader<String> resource(String resourceName) {
        return new ClientStreamResourceReader<>() {
            @Override
            public String resourceName() {
                return resourceName;
            }

            @Override
            public String resource(String resourceId) {
                return "claim:" + resourceId;
            }
        };
    }
}
