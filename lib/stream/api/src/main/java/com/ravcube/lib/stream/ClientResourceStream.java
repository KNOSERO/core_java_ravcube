package com.ravcube.lib.stream;

import java.util.Objects;

public interface ClientResourceStream<T> {

    String resourceName();

    ClientStreamPublisher publisher();

    default void refresh(String resourceId, T payload) {
        publisher().refresh(
                Objects.requireNonNull(resourceName(), "resourceName must not be null"),
                Objects.requireNonNull(resourceId, "resourceId must not be null"),
                Objects.requireNonNull(payload, "payload must not be null")
        );
    }
}
