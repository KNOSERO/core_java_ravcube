package com.ravcube.lib.event.publisher;

import com.ravcube.lib.event.DomainEvent;
import com.ravcube.lib.event.inteface.EventListener;
import com.ravcube.lib.event.enums.EventSource;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultRollbackPublisher<E extends DomainEvent> extends AbstractRollbackPublisher<E> {

    private EventListener eventListener;

    @Override
    protected void on(E event) {
        eventListener.on(source(), event);
    }

    @Override
    public EventSource source() {
        return EventSource.SPRING_AFTER_ROLLBACK;
    }

    @Autowired
    private void setRoutingEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }


}
