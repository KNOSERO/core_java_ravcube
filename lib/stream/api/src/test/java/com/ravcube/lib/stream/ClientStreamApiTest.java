package com.ravcube.lib.stream;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientStreamApiTest {

    @Test
    void shouldLoadAndPublishSingleResourceUpdate() {
        final RecordingPublisher publisher = new RecordingPublisher();
        final ClientRestResourceStream<String> stream = new ClientRestResourceStream<>() {
            @Override
            public String resourceName() {
                return "policies.claims";
            }

            @Override
            public ClientStreamPublisher publisher() {
                return publisher;
            }

            @Override
            public String resource(String resourceId) {
                return "claim:" + resourceId;
            }
        };

        assertTrue(stream.update("claim-1"));

        assertEquals(
                List.of("policies.claims:claim-1:claim:claim-1"),
                publisher.calls
        );
    }

    @Test
    void shouldNotPublishWhenResourceDoesNotExist() {
        final RecordingPublisher publisher = new RecordingPublisher();
        final ClientRestResourceStream<String> stream = new ClientRestResourceStream<>() {
            @Override
            public String resourceName() {
                return "policies.claims";
            }

            @Override
            public ClientStreamPublisher publisher() {
                return publisher;
            }

            @Override
            public String resource(String resourceId) {
                return null;
            }
        };

        assertFalse(stream.update("claim-1"));
        assertEquals(List.of(), publisher.calls);
    }

    private static final class RecordingPublisher implements ClientStreamPublisher {

        private final List<String> calls = new ArrayList<>();

        @Override
        public void publish(String resourceName, String resourceId, Object payload) {
            calls.add(resourceName + ":" + resourceId + ":" + payload);
        }
    }
}
