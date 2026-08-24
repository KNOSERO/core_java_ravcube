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
import java.util.Optional;

@RestController
@RequestMapping("${ravcube.stream.path:/streams}")
public final class ClientStreamController {

    private final ClientStreamRegistry registry;
    private final List<ClientRestResourceStream<?>> resourceStreams;

    public ClientStreamController(
            ClientStreamRegistry registry,
            List<ClientRestResourceStream<?>> resourceStreams
    ) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        this.resourceStreams = List.copyOf(resourceStreams);
    }

    @GetMapping(value = "/{resourceName}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeResources(
            @PathVariable String resourceName,
            @RequestParam(name = "ids") List<String> resourceIds
    ) {
        try {
            return registry.subscribe(resourceName, resourceIds);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    exception.getMessage(),
                    exception
            );
        }
    }

    @GetMapping(value = "/{resourceName}/{resourceId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeResource(
            @PathVariable String resourceName,
            @PathVariable String resourceId
    ) {
        final Optional<ClientRestResourceStream<?>> stream = findResourceStream(resourceName);
        final Object initialPayload = stream
                .map(resourceHandler -> resourceHandler.resource(resourceId))
                .orElse(null);

        final SseEmitter emitter = registry.subscribe(resourceName, List.of(resourceId));
        if (initialPayload != null) {
            registry.sendInitial(emitter, initialPayload);
        }
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

        if (!stream.update(resourceId)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    private Optional<ClientRestResourceStream<?>> findResourceStream(String resourceName) {
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
