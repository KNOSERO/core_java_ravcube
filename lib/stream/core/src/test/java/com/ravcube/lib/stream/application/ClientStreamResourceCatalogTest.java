package com.ravcube.lib.stream.application;

import com.ravcube.lib.stream.api.ClientRestResourceStream;
import com.ravcube.lib.stream.api.ClientStreamPublisher;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ClientStreamResourceCatalogTest {

    @Test
    void shouldRejectDuplicateResourceNames() {
        final ClientRestResourceStream<String> first = stream("claims");
        final ClientRestResourceStream<String> second = stream("claims");

        assertThrows(
                IllegalStateException.class,
                () -> new ClientStreamResourceCatalog(List.of(first, second))
        );
    }

    private static ClientRestResourceStream<String> stream(String resourceName) {
        return new ClientRestResourceStream<>() {
            @Override
            public String resourceName() {
                return resourceName;
            }

            @Override
            public ClientStreamPublisher publisher() {
                return (name, id, payload) -> { };
            }

            @Override
            public String resource(String resourceId) {
                return resourceId;
            }
        };
    }
}
