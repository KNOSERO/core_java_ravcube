package com.ravcube.lib.stream.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientStreamResourceReaderTest {

    @Test
    void resourceIsLoadedById() {
        final ClientStreamResourceReader<String> stream = new ClientStreamResourceReader<>() {
            @Override
            public String resourceName() {
                return "policies.claims";
            }

            @Override
            public String resource(String resourceId) {
                return "claim:" + resourceId;
            }
        };

        assertEquals("claim:claim-1", stream.resource("claim-1"));
    }
}
