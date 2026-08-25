package com.ravcube.lib.event.domain;

import com.ravcube.lib.event.DomainEvent;
import com.ravcube.lib.event.annotation.Topic;

import java.util.UUID;

@Topic("nats.event")
public record NatsDomainEvent(UUID id, String message) implements DomainEvent {
}
