package com.ravcube.lib.stream.web;

import com.ravcube.lib.stream.application.ClientStreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    private final ClientStreamService service;

    @Autowired
    public ClientStreamController(ClientStreamService service) {
        this.service = Objects.requireNonNull(service, "service must not be null");
    }

    @GetMapping(value = "/{resourceName}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeResources(
            @PathVariable String resourceName,
            @RequestParam(name = "ids") List<String> resourceIds
    ) {
        return subscribe(() -> service.subscribe(resourceName, resourceIds));
    }

    @GetMapping(value = "/{resourceName}/{resourceId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeResource(
            @PathVariable String resourceName,
            @PathVariable String resourceId
    ) {
        return subscribe(() -> service.subscribe(resourceName, resourceId));
    }

    private static SseEmitter subscribe(java.util.function.Supplier<SseEmitter> action) {
        try {
            return action.get();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    exception.getMessage(),
                    exception
            );
        }
    }
}
