package com.ravcube.lib.event.inteface;

import com.ravcube.lib.event.DomainEvent;

import java.util.List;

public interface EventPublisher {

    void publish(DomainEvent event);

    void publish(List<DomainEvent> event);
}
