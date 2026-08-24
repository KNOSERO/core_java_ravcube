package com.ravcube.lib.stream;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClientStreamRegistryTest {

    @Test
    void shouldPublishOnlyToSubscriptionsContainingUpdatedId() {
        final ClientStreamProperties properties = new ClientStreamProperties(Duration.ofMinutes(10));
        final RecordingEmitter first = new RecordingEmitter();
        final RecordingEmitter firstAndSecond = new RecordingEmitter();
        final RecordingEmitter second = new RecordingEmitter();
        final Deque<SseEmitter> emitters = new ArrayDeque<>(
                List.of(first, firstAndSecond, second)
        );
        final ClientStreamRegistry registry = new ClientStreamRegistry(
                properties,
                timeout -> emitters.removeFirst()
        );

        registry.subscribe("claims", List.of("1"));
        registry.subscribe("claims", List.of("1", "2"));
        registry.subscribe("claims", List.of("2"));

        registry.publish("claims", "1", "claim-1");

        assertEquals(1, first.events);
        assertEquals(1, firstAndSecond.events);
        assertEquals(0, second.events);
    }

    @Test
    void shouldRequireAtLeastOneResourceId() {
        final ClientStreamProperties properties = new ClientStreamProperties(Duration.ofMinutes(10));
        final ClientStreamRegistry registry = new ClientStreamRegistry(
                properties,
                timeout -> new RecordingEmitter()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> registry.subscribe("claims", List.of())
        );
    }

    private static final class RecordingEmitter extends SseEmitter {

        private int events;

        @Override
        public void send(SseEventBuilder builder) throws IOException {
            events++;
        }
    }
}
