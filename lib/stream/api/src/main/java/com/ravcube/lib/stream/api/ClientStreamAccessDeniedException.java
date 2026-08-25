package com.ravcube.lib.stream.api;

public final class ClientStreamAccessDeniedException extends RuntimeException {

    public ClientStreamAccessDeniedException(String resourceName) {
        super("Access denied for stream resource: " + resourceName);
    }
}
