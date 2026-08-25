package com.ravcube.lib.stream.application;

import com.ravcube.lib.stream.api.ClientRestResourceStream;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public final class ClientStreamResourceCatalog {

    private final Map<String, ClientRestResourceStream<?>> resourceStreams;

    public ClientStreamResourceCatalog(List<ClientRestResourceStream<?>> resourceStreams) {
        this.resourceStreams = indexResourceStreams(resourceStreams);
    }

    public Optional<ClientRestResourceStream<?>> find(String resourceName) {
        return Optional.ofNullable(resourceStreams.get(resourceName));
    }

    private static Map<String, ClientRestResourceStream<?>> indexResourceStreams(
            List<ClientRestResourceStream<?>> streams
    ) {
        Objects.requireNonNull(streams, "resourceStreams must not be null");
        final Map<String, ClientRestResourceStream<?>> indexed = new HashMap<>();
        for (ClientRestResourceStream<?> stream : streams) {
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
