package com.ravcube.lib.event.domain;

import java.util.Objects;

public record EventExecutionId<K>(K value) {

    public EventExecutionId {
        Objects.requireNonNull(value, "event execution id cannot be null");
    }

    public static <K> EventExecutionId<K> of(K value) {
        return new EventExecutionId<>(value);
    }
}
