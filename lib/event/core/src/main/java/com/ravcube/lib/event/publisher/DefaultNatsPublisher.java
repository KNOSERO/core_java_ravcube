package com.ravcube.lib.event.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ravcube.lib.event.DomainEvent;
import com.ravcube.lib.event.enums.EventSource;
import com.ravcube.lib.event.nats.NatsPublishSupport;
import com.ravcube.lib.event.nats.NatsSubjectResolver;
import io.nats.client.Connection;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

public class DefaultNatsPublisher<E extends DomainEvent> extends AbstractCommitPublisher<E> {

    private NatsPublishSupport natsPublisher;

    @Override
    protected void on(E event) {
        Objects.requireNonNull(natsPublisher, "natsPublisher must not be null").publish(event);
    }

    @Override
    public EventSource source() {
        return EventSource.NATS_BROADCAST;
    }

    @Autowired
    private void setNatsPublisher(
            Connection connection,
            ObjectProvider<ObjectMapper> objectMappers,
            NatsSubjectResolver subjectResolver
    ) {
        ObjectMapper objectMapper = objectMappers.getIfAvailable(DefaultNatsPublisher::defaultObjectMapper);
        this.natsPublisher = new NatsPublishSupport(connection, objectMapper, subjectResolver);
    }

    private static ObjectMapper defaultObjectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
