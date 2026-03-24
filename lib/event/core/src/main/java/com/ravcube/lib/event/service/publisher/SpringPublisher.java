package com.ravcube.lib.event.service.publisher;

import com.ravcube.lib.event.DomainEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

class SpringPublisher<E extends DomainEvent> {

    private ApplicationEventPublisher applicationEventPublisher;

    public void publish(E event) {
        applicationEventPublisher.publishEvent(event);
    }

    @Autowired
    private void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
