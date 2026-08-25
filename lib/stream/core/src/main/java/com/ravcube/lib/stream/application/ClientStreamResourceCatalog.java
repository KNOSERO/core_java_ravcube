package com.ravcube.lib.stream.application;

import com.ravcube.lib.stream.api.ClientStreamResourceReader;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public final class ClientStreamResourceCatalog {

    private final Map<String, ClientStreamResourceReader<?>> resourceStreams;

    public ClientStreamResourceCatalog(List<ClientStreamResourceReader<?>> resourceStreams) {
        this.resourceStreams = indexResourceStreams(resourceStreams);
    }

    public Optional<ClientStreamResourceReader<?>> find(String resourceName) {
        return Optional.ofNullable(resourceStreams.get(resourceName));
    }

    private static Map<String, ClientStreamResourceReader<?>> indexResourceStreams(
            List<ClientStreamResourceReader<?>> streams
    ) {
        Objects.requireNonNull(streams, "resourceStreams must not be null");
        final Map<String, ClientStreamResourceReader<?>> indexed = new HashMap<>();
        for (ClientStreamResourceReader<?> stream : streams) {
            Objects.requireNonNull(stream, "resource stream must not be null");
            final String resourceName = Objects.requireNonNull(
                    stream.resourceName(),
                    "resourceName must not be null"
            );
            if (resourceName.isBlank()) {
                throw new IllegalStateException("resourceName must not be blank");
            }
            if (indexed.putIfAbsent(resourceName, stream) != null) {
                throw new IllegalStateException(
                        "More than one stream resource handler registered for: " + resourceName
                );
            }
        }
        return Map.copyOf(indexed);
    }
}
