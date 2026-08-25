package com.ravcube.lib.event.publisher;

import com.ravcube.lib.event.domain.NatsDomainEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("nats")
public class NatsCommitPublisher extends DefaultNatsPublisher<NatsDomainEvent> {
}
