package com.ravcube.lib.stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;

@Component
public final class ClientStreamProperties {

    private final String path;
    private final Duration timeout;

    public ClientStreamProperties(
            @Value("${ravcube.stream.path:/streams}") String path,
            @Value("${ravcube.stream.timeout:PT30M}") Duration timeout
    ) {
        this.path = normalizePath(path);
        this.timeout = validateTimeout(timeout);
    }

    public String path() {
        return path;
    }

    public Duration timeout() {
        return timeout;
    }

    private static String normalizePath(String value) {
        Objects.requireNonNull(value, "path must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("path must not be blank");
        }

        String normalized = value.startsWith("/") ? value : "/" + value;
        while (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private static Duration validateTimeout(Duration value) {
        Objects.requireNonNull(value, "timeout must not be null");
        if (value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException("timeout must be greater than zero");
        }
        return value;
    }
}
