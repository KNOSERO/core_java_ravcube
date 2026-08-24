package com.ravcube.lib.stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;

@Component
public final class ClientStreamProperties {

    private final Duration timeout;

    public ClientStreamProperties(
            @Value("${ravcube.stream.timeout:PT30M}") Duration timeout
    ) {
        this.timeout = validateTimeout(timeout);
    }

    public Duration timeout() {
        return timeout;
    }

    private static Duration validateTimeout(Duration value) {
        Objects.requireNonNull(value, "timeout must not be null");
        if (value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException("timeout must be greater than zero");
        }
        return value;
    }
}
