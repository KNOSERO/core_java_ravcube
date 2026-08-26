package com.ravcube.lib.event.publisher;

import com.ravcube.lib.event.DomainEvent;
import com.ravcube.lib.event.routing.EventRouter;
import com.ravcube.lib.event.routing.EventSource;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultCommitPublisher<E extends DomainEvent> extends AbstractCommitPublisher<E> {

    private EventRouter eventRouter;

    @Override
    protected void on(E event) {
        eventRouter.on(source(), event);
    }

    @Override
    public EventSource source() {
        return EventSource.SPRING_AFTER_COMMIT;
    }

    @Autowired
    private void setEventRouter(EventRouter eventRouter) {
        this.eventRouter = eventRouter;
    }
}
