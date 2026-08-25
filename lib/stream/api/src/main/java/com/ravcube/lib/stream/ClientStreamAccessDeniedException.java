package com.ravcube.lib.stream;

public final class ClientStreamAccessDeniedException extends RuntimeException {

    public ClientStreamAccessDeniedException(String resourceName) {
        super("Access denied for stream resource: " + resourceName);
    }
}
