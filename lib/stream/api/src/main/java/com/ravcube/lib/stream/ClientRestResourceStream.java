package com.ravcube.lib.stream;

public interface ClientRestResourceStream<T> extends ClientResourceStream<T> {

    T resource(String resourceId);

    default void update(String resourceId) {
        refresh(resourceId, resource(resourceId));
    }
}
