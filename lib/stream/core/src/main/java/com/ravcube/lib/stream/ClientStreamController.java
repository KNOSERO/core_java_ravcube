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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("${ravcube.stream.path:/streams}")
public final class ClientStreamController {

    private final ClientStreamRegistry registry;
    private final Map<String, ClientRestResourceStream<?>> resourceStreams;

    public ClientStreamController(
            ClientStreamRegistry registry,
            List<ClientRestResourceStream<?>> resourceStreams
    ) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        this.resourceStreams = indexResourceStreams(resourceStreams);
    }

    @GetMapping(value = "/{resourceName}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeResources(
            @PathVariable String resourceName,
            @RequestParam(name = "ids") List<String> resourceIds
    ) {
        try {
            return registry.subscribe(resourceName, resourceIds);
        } catch (ClientStreamAccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage(), exception);
        } catch (ClientStreamLimitExceededException exception) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, exception.getMessage(), exception);
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
        final SseEmitter emitter;
        try {
            emitter = registry.subscribe(resourceName, List.of(resourceId));
        } catch (ClientStreamAccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage(), exception);
        } catch (ClientStreamLimitExceededException exception) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, exception.getMessage(), exception);
        }

        try {
            final Object initialPayload = findResourceStream(resourceName)
                    .map(resourceHandler -> resourceHandler.resource(resourceId))
                    .orElse(null);
            if (initialPayload != null) {
                registry.sendInitial(emitter, resourceName, resourceId, initialPayload);
            }
        } catch (ClientStreamAccessDeniedException exception) {
            registry.unsubscribe(emitter);
            emitter.completeWithError(exception);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage(), exception);
        } catch (RuntimeException exception) {
            registry.unsubscribe(emitter);
            emitter.completeWithError(exception);
            throw exception;
        }
        return emitter;
    }

    @PostMapping("/updates/{resourceName}/{resourceId}")
    public ResponseEntity<Void> updateResource(
            @PathVariable String resourceName,
            @PathVariable String resourceId
    ) {
        try {
            registry.assertAuthorized(resourceName, resourceId);
        } catch (ClientStreamAccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage(), exception);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }

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
