package com.ravcube.lib.stream;

public final class ClientStreamLimitExceededException extends RuntimeException {

    public ClientStreamLimitExceededException(String message) {
        super(message);
    }
}
