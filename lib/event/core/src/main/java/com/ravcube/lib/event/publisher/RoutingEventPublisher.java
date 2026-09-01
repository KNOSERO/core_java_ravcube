package com.ravcube.lib.event.publisher;

import com.ravcube.lib.event.DomainEvent;
import com.ravcube.lib.event.api.EventPublisher;
import com.ravcube.lib.event.routing.AbstractEventPublisher;
import com.ravcube.lib.event.routing.EventPublisherRegistry;

import java.util.List;

public final class RoutingEventPublisher implements EventPublisher {

    private final EventPublisherRegistry publishers;

    public RoutingEventPublisher(List<AbstractEventPublisher<? extends DomainEvent>> publishers) {
        this.publishers = EventPublisherRegistry.of(publishers);
    }

    @Override
    public void publish(DomainEvent event) {
        publishers.publish(event);
    }

    @Override
    public void publish(List<? extends DomainEvent> events) {
        events.forEach(this::publish);
    }
}
