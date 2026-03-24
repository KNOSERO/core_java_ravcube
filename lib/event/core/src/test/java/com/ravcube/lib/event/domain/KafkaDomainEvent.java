package com.ravcube.lib.event.domain;

import com.ravcube.lib.event.DomainEvent;
import com.ravcube.lib.event.annotation.Topic;

import java.util.UUID;

@Topic("kafka.event")
public record KafkaDomainEvent(UUID id, String message) implements DomainEvent {
}