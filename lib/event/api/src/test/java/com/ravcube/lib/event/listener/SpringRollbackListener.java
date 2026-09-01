package com.ravcube.lib.event.listener;

import com.ravcube.lib.event.domain.SpringDomainEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SpringRollbackListener extends DefaultRollbackListener<SpringDomainEvent> {

    private static final EventInvocationTracker<SpringDomainEvent, UUID> LEDGER = EventInvocationTracker.of(SpringDomainEvent::id);

    @Override
    public void on(SpringDomainEvent event) {
        LEDGER.register(event);
    }

    public static void reset() {
        LEDGER.reset();
    }

    public static int invocations(UUID eventId) {
        return LEDGER.invocations(eventId);
    }
}
