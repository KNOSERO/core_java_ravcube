package com.ravcube.lib.stream.common;

public final class ClientStreamCapacityExceededException extends RuntimeException {

    public ClientStreamCapacityExceededException(String message) {
        super(message);
    }
}
