package com.ravcube.lib.stream.application;

public final class ClientStreamAccessDeniedException extends RuntimeException {

    public ClientStreamAccessDeniedException(String resourceName, String resourceId) {
        super("Access denied for stream resource " + resourceName + "/" + resourceId);
    }
}
