package com.ravcube.lib.stream;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("${ravcube.stream.path:/streams}")
public final class ClientStreamController {

    private final ClientStreamRegistry registry;
    private final ClientStreamPublisher publisher;
    private final List<ClientRestResourceStream<?>> resourceStreams;

    public ClientStreamController(
            ClientStreamRegistry registry,
            ClientStreamPublisher publisher,
            List<ClientRestResourceStream<?>> resourceStreams
    ) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
        this.resourceStreams = List.copyOf(resourceStreams);
    }

    @GetMapping(value = "/{resourceName}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeCollection(
            @PathVariable String resourceName,
            @RequestParam(name = "ids", required = false) List<String> resourceIds
    ) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return registry.subscribeCollection(resourceName);
        }
        return registry.subscribeSelectedCollection(resourceName, resourceIds);
    }

    @GetMapping(value = "/{resourceName}/{resourceId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeResource(
            @PathVariable String resourceName,
            @PathVariable String resourceId
    ) {
        final SseEmitter emitter = registry.subscribeResource(resourceName, resourceId);
        findResourceStream(resourceName).ifPresent(stream -> {
            final Object payload = stream.resource(resourceId);
            if (payload != null) {
                registry.sendInitial(emitter, payload);
            }
        });
        return emitter;
    }

    @PostMapping("/updates/{resourceName}/{resourceId}")
    public ResponseEntity<Void> updateResource(
            @PathVariable String resourceName,
            @PathVariable String resourceId
    ) {
        final ClientRestResourceStream<?> stream = findResourceStream(resourceName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No stream resource handler registered for: " + resourceName
                ));

        final Object payload = stream.resource(resourceId);
        if (payload == null) {
            return ResponseEntity.notFound().build();
        }

        publisher.refresh(resourceName, resourceId, payload);
        return ResponseEntity.noContent().build();
    }

    private java.util.Optional<ClientRestResourceStream<?>> findResourceStream(String resourceName) {
        final List<ClientRestResourceStream<?>> matches = resourceStreams.stream()
                .filter(stream -> resourceName.equals(stream.resourceName()))
                .toList();

        if (matches.size() > 1) {
            throw new IllegalStateException(
                    "More than one stream resource handler registered for: " + resourceName
            );
        }
        return matches.stream().findFirst();
    }
}
