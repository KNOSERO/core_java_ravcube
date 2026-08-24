package com.ravcube.lib.stream;

import java.util.Collection;
import java.util.Objects;

public interface ClientSelectedCollectionStream<T> {

    String resourceName();

    ClientStreamPublisher publisher();

    default void refresh(Collection<String> resourceIds, T payload) {
        publisher().refresh(
                Objects.requireNonNull(resourceName(), "resourceName must not be null"),
                Objects.requireNonNull(resourceIds, "resourceIds must not be null"),
                Objects.requireNonNull(payload, "payload must not be null")
        );
    }
}
