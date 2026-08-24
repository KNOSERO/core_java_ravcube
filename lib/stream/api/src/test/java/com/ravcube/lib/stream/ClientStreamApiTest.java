package com.ravcube.lib.stream;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientStreamApiTest {

    @Test
    void shouldPublishSingleResourceRefresh() {
        final RecordingPublisher publisher = new RecordingPublisher();
        final ClientResourceStream<String> stream = new ClientResourceStream<>() {
            @Override
            public String resourceName() {
                return "policies.claims";
            }

            @Override
            public ClientStreamPublisher publisher() {
                return publisher;
            }
        };

        stream.refresh("claim-1", "claim");

        assertEquals(List.of("resource:policies.claims:claim-1:claim"), publisher.calls);
    }

    @Test
    void shouldPublishCollectionAndSelectedCollectionRefreshes() {
        final RecordingPublisher publisher = new RecordingPublisher();
        final ClientCollectionStream<String> collection = new ClientCollectionStream<>() {
            @Override
            public String resourceName() {
                return "policies.claims";
            }

            @Override
            public ClientStreamPublisher publisher() {
                return publisher;
            }
        };
        final ClientSelectedCollectionStream<String> selected = new ClientSelectedCollectionStream<>() {
            @Override
            public String resourceName() {
                return "policies.claims";
            }

            @Override
            public ClientStreamPublisher publisher() {
                return publisher;
            }
        };

        collection.refresh("claims");
        selected.refresh(List.of("claim-1", "claim-2"), "claims");

        assertEquals(
                List.of(
                        "collection:policies.claims:claims",
                        "selected:policies.claims:[claim-1, claim-2]:claims"
                ),
                publisher.calls
        );
    }

    @Test
    void shouldLoadResourceBeforePublishingUpdate() {
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

        stream.update("claim-1");

        assertEquals(List.of("resource:policies.claims:claim-1:claim:claim-1"), publisher.calls);
    }

    @Test
    void shouldCreateStableSelectedCollectionName() {
        assertEquals(
                "policies.claims.claim-1,claim-2",
                ClientStreamNames.selectedCollection(
                        "policies.claims",
                        List.of("claim-2", "claim-1", "claim-2")
                )
        );
    }

    private static final class RecordingPublisher implements ClientStreamPublisher {

        private final List<String> calls = new ArrayList<>();

        @Override
        public <T> void refresh(String resourceName, String resourceId, T payload) {
            calls.add("resource:" + resourceName + ":" + resourceId + ":" + payload);
        }

        @Override
        public <T> void refresh(String resourceName, T payload) {
            calls.add("collection:" + resourceName + ":" + payload);
        }

        @Override
        public <T> void refresh(String resourceName, Collection<String> resourceIds, T payload) {
            calls.add("selected:" + resourceName + ":" + resourceIds + ":" + payload);
        }
    }
}
