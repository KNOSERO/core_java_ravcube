package com.ravcube.lib.stream.application;

import com.ravcube.lib.stream.api.ClientStreamResourceReader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ClientStreamResourceCatalogTest {

    @Test
    void duplicateResourceNamesAreRejected() {
        final ClientStreamResourceReader<String> first = stream("claims");
        final ClientStreamResourceReader<String> second = stream("claims");

        assertThrows(
                IllegalStateException.class,
                () -> new ClientStreamResourceCatalog(List.of(first, second))
        );
    }

    private static ClientStreamResourceReader<String> stream(String resourceName) {
        return new ClientStreamResourceReader<>() {
            @Override
            public String resourceName() {
                return resourceName;
            }

            @Override
            public String resource(String resourceId) {
                return resourceId;
            }
        };
    }
}
