package com.ravcube.lib.event.listener;

import com.ravcube.lib.event.domain.SpringDomainEvent;
import com.ravcube.lib.event.domain.execution.EventExecutionLedger;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SpringCommitListener extends DefaultCommitListener<SpringDomainEvent> {

    private static final EventExecutionLedger<SpringDomainEvent, UUID> LEDGER = EventExecutionLedger.of(SpringDomainEvent::id);

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
