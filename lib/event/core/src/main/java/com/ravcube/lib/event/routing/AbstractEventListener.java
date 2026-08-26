package com.ravcube.lib.event.routing;

import com.ravcube.lib.event.DomainEvent;
import org.springframework.core.ResolvableType;

public interface AbstractEventListener<E extends DomainEvent> {

    EventSource source();

    void on(E event);

    @SuppressWarnings("unchecked")
    default Class<E> eventType() {
        return (Class<E>) ResolvableType.forClass(getClass())
                .as(AbstractEventListener.class)
                .getGeneric(0)
                .resolve();
    }
}
