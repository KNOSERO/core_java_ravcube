package com.ravcube.lib.event.nats;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ravcube.lib.event.DomainEvent;
import com.ravcube.lib.event.enums.EventSource;
import com.ravcube.lib.event.inteface.AbstractListener;
import com.ravcube.lib.event.inteface.EventListener;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Profile("nats")
final class DefaultNatsListener implements SmartLifecycle {

    private static final Logger LOGGER = Logger.getLogger(DefaultNatsListener.class.getName());

    private final Connection connection;
    private final ObjectMapper objectMapper;
    private final EventListener eventListener;
    private final Map<String, Class<? extends DomainEvent>> eventTypesBySubject;
    private Dispatcher dispatcher;
    private volatile boolean running;

    DefaultNatsListener(
            Connection connection,
            ObjectProvider<ObjectMapper> objectMappers,
            EventListener eventListener,
            List<AbstractListener<? extends DomainEvent>> listeners,
            NatsSubjectResolver subjectResolver
    ) {
        this.connection = Objects.requireNonNull(connection, "connection must not be null");
        this.objectMapper = objectMappers.getIfAvailable(DefaultNatsListener::defaultObjectMapper);
        this.eventListener = Objects.requireNonNull(eventListener, "eventListener must not be null");
        this.eventTypesBySubject = listeners.stream()
                .filter(listener -> listener.source() == EventSource.NATS_BROADCAST)
                .map(AbstractListener::domainClass)
                .collect(Collectors.toUnmodifiableMap(
                        subjectResolver::subject,
                        Function.identity(),
                        (first, ignored) -> first
                ));
    }

    @Override
    public void start() {
        if (running) {
            return;
        }

        dispatcher = connection.createDispatcher(this::handleMessage);
        eventTypesBySubject.keySet().forEach(dispatcher::subscribe);
        running = true;
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }

        eventTypesBySubject.keySet().forEach(dispatcher::unsubscribe);
        connection.closeDispatcher(dispatcher);
        dispatcher = null;
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    private void handleMessage(Message message) {
        Class<? extends DomainEvent> eventType = eventTypesBySubject.get(message.getSubject());
        if (eventType == null) {
            LOGGER.fine(() -> "Ignoring NATS message for unknown subject " + message.getSubject());
            return;
        }

        try {
            DomainEvent event = objectMapper.readValue(message.getData(), eventType);
            eventListener.on(EventSource.NATS_BROADCAST, event);
        } catch (IOException | RuntimeException exception) {
            LOGGER.log(Level.WARNING, "Could not process NATS event from subject " + message.getSubject(), exception);
        }
    }

    private static ObjectMapper defaultObjectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
