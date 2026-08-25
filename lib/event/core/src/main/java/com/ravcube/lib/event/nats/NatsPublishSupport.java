package com.ravcube.lib.event.nats;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ravcube.lib.event.DomainEvent;
import io.nats.client.Connection;

import java.util.Objects;

public final class NatsPublishSupport {

    private final Connection connection;
    private final ObjectMapper objectMapper;
    private final NatsSubjectResolver subjectResolver;

    public NatsPublishSupport(
            Connection connection,
            ObjectMapper objectMapper,
            NatsSubjectResolver subjectResolver
    ) {
        this.connection = Objects.requireNonNull(connection, "connection must not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        this.subjectResolver = Objects.requireNonNull(subjectResolver, "subjectResolver must not be null");
    }

    public void publish(DomainEvent event) {
        DomainEvent payload = Objects.requireNonNull(event, "event must not be null");
        try {
            connection.publish(
                    subjectResolver.subject(payload.getClass()),
                    objectMapper.writeValueAsBytes(payload)
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize NATS event", exception);
        }
    }
}
