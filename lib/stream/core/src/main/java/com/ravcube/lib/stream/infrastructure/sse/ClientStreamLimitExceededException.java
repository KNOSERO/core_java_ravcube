package com.ravcube.lib.stream.infrastructure.sse;

final class ClientStreamLimitExceededException extends IllegalArgumentException {

    ClientStreamLimitExceededException(String message) {
        super(message);
    }
}
