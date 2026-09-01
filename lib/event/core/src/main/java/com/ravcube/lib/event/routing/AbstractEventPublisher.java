package com.ravcube.lib.event.routing;

import com.ravcube.lib.event.DomainEvent;
import org.springframework.core.ResolvableType;

public interface AbstractEventPublisher<E extends DomainEvent> {

    EventSource source();

    void publish(E event);

    @SuppressWarnings("unchecked")
    default Class<E> eventType() {
        return (Class<E>) ResolvableType.forClass(getClass())
                .as(AbstractEventPublisher.class)
                .getGeneric(0)
                .resolve();
    }
}
