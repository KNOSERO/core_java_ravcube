package com.ravcube.lib.stream.infrastructure.sse;

import com.ravcube.lib.stream.infrastructure.config.ClientStreamProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultClientStreamPublisherTest {

    @AfterEach
    void clearTransactionState() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    void shouldPublishOnlyAfterTransactionCommit() {
        final RecordingEmitter emitter = new RecordingEmitter();
        final ClientStreamRegistry registry = new ClientStreamRegistry(
                new ClientStreamProperties(Duration.ofMinutes(10)),
                (resourceName, resourceIds) -> resourceId -> true,
                timeout -> emitter
        );
        registry.subscribe("claims", java.util.List.of("1"));
        final DefaultClientStreamPublisher publisher = new DefaultClientStreamPublisher(registry);

        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        publisher.publish("claims", "1", "claim-1");

        assertEquals(0, emitter.events);
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(synchronization -> synchronization.afterCommit());
        assertEquals(1, emitter.events);
    }

    private static final class RecordingEmitter extends SseEmitter {

        private int events;

        @Override
        public void send(SseEventBuilder builder) throws IOException {
            events++;
        }
    }
}
