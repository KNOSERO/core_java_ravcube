package com.ravcube.lib.event.nats;

import com.ravcube.lib.event.DomainEvent;

import java.util.Objects;

public final class NatsSubjectResolver {

    private final String subjectPrefix;

    NatsSubjectResolver(NatsProperties properties) {
        this.subjectPrefix = validatePrefix(properties.getSubjectPrefix());
    }

    public String subject(Class<? extends DomainEvent> eventType) {
        Objects.requireNonNull(eventType, "eventType must not be null");
        return subjectPrefix + "." + DomainEvent.getTopic(eventType);
    }

    private String validatePrefix(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ravcube.nats.subject-prefix must not be blank");
        }
        if (value.indexOf('*') >= 0 || value.indexOf('>') >= 0 || value.matches(".*\\s+.*")) {
            throw new IllegalArgumentException(
                    "ravcube.nats.subject-prefix must not contain whitespace, '*' or '>'"
            );
        }
        return value.endsWith(".") ? value.substring(0, value.length() - 1) : value;
    }
}
