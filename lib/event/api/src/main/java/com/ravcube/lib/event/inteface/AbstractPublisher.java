package com.ravcube.lib.event.inteface;

import com.ravcube.lib.event.DomainEvent;
import com.ravcube.lib.event.enums.EventSource;
import org.springframework.core.ResolvableType;

public interface AbstractPublisher<E extends DomainEvent>  {

    EventSource source();

    void publish(E event);

    @SuppressWarnings("unchecked")
    default Class<E> domainClass() {
        return (Class<E>) ResolvableType.forClass(getClass())
                .as(AbstractPublisher.class)
                .getGeneric(0)
                .resolve();
    }
}
