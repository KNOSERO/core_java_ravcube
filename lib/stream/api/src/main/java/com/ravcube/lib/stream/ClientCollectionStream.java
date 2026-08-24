package com.ravcube.lib.stream;

import java.util.Objects;

public interface ClientCollectionStream<T> {

    String resourceName();

    ClientStreamPublisher publisher();

    default void refresh(T payload) {
        publisher().refresh(
                Objects.requireNonNull(resourceName(), "resourceName must not be null"),
                Objects.requireNonNull(payload, "payload must not be null")
        );
    }
}
