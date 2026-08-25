package com.ravcube.lib.event.listener;

import com.ravcube.lib.event.domain.NatsDomainEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@Profile("nats")
public class NatsCommitListenerPodOne extends DefaultNatsCommitListener<NatsDomainEvent> {

    private static final EventInvocationTracker<NatsDomainEvent, UUID> LEDGER =
            EventInvocationTracker.of(NatsDomainEvent::id);

    @Override
    public void on(NatsDomainEvent event) {
        LEDGER.register(event);
    }

    public static void reset() {
        LEDGER.reset();
    }

    public static int invocations(UUID eventId) {
        return LEDGER.invocations(eventId, Duration.ofSeconds(5));
    }
}
