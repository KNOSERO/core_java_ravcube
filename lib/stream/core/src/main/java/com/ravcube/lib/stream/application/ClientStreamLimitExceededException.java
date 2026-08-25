package com.ravcube.lib.stream.application;

public final class ClientStreamLimitExceededException extends RuntimeException {

    public ClientStreamLimitExceededException(String message) {
        super(message);
    }
}
