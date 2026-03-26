package com.ravcube.lib.event.listener;

import com.ravcube.lib.event.domain.KafkaDomainEvent;
import com.ravcube.lib.event.domain.execution.EventExecutionLedger;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
public class KafkaCommitAuditListener extends DefaultKafkaCommitListener<KafkaDomainEvent> {

    private static final EventExecutionLedger<KafkaDomainEvent, UUID> LEDGER = EventExecutionLedger.of(KafkaDomainEvent::id);

    @Override
    public void on(KafkaDomainEvent event) {
        LEDGER.register(event);
    }

    public static void reset() {
        LEDGER.reset();
    }

    public static int invocations(UUID eventId) {
        return LEDGER.invocations(eventId, Duration.ofSeconds(5));
    }
}
