package com.ravcube.lib.event.domain;

import com.ravcube.lib.event.DomainEvent;

import java.util.UUID;

public record SpringDomainEvent(UUID id, String message) implements DomainEvent {
}
