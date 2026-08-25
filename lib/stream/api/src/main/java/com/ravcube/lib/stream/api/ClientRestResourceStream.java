package com.ravcube.lib.stream.api;

import java.util.Objects;

public interface ClientRestResourceStream<T> {

    String resourceName();

    ClientStreamPublisher publisher();

    T resource(String resourceId);

    default boolean update(String resourceId) {
        final String validatedResourceName = Objects.requireNonNull(
                resourceName(),
                "resourceName must not be null"
        );
        final String validatedResourceId = Objects.requireNonNull(
                resourceId,
                "resourceId must not be null"
        );
        final T payload = resource(validatedResourceId);

        if (payload == null) {
            return false;
        }

        Objects.requireNonNull(publisher(), "publisher must not be null")
                .publish(validatedResourceName, validatedResourceId, payload);
        return true;
    }
}
