package com.ravcube.lib.stream.web;

import com.ravcube.lib.stream.api.ClientRestResourceStream;
import com.ravcube.lib.stream.api.ClientStreamAccessDeniedException;
import com.ravcube.lib.stream.application.ClientStreamLimitExceededException;
import com.ravcube.lib.stream.application.ClientStreamResourceCatalog;
import com.ravcube.lib.stream.application.ClientStreamUpdateService;
import com.ravcube.lib.stream.infrastructure.sse.ClientStreamRegistry;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final ClientStreamResourceCatalog resourceCatalog;
    private final ClientStreamUpdateService updateService;

    @Autowired
    public ClientStreamController(
            ClientStreamRegistry registry,
            ClientStreamResourceCatalog resourceCatalog,
            ClientStreamUpdateService updateService
    ) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        this.resourceCatalog = Objects.requireNonNull(resourceCatalog, "resourceCatalog must not be null");
        this.updateService = Objects.requireNonNull(updateService, "updateService must not be null");
    }

    public ClientStreamController(
            ClientStreamRegistry registry,
            List<ClientRestResourceStream<?>> resourceStreams
    ) {
        this(registry, new ClientStreamResourceCatalog(resourceStreams));
    }

    private ClientStreamController(
            ClientStreamRegistry registry,
            ClientStreamResourceCatalog resourceCatalog
    ) {
        this(registry, resourceCatalog, new ClientStreamUpdateService(resourceCatalog));
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
            final Object initialPayload = resourceCatalog.find(resourceName)
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

        if (!updateService.update(resourceName, resourceId)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

}
