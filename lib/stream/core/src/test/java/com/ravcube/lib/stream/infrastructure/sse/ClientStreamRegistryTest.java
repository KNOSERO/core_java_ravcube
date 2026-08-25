package com.ravcube.lib.stream.infrastructure.sse;

import com.ravcube.lib.stream.api.ClientStreamAccessDeniedException;
import com.ravcube.lib.stream.application.ClientStreamLimitExceededException;
import com.ravcube.lib.stream.infrastructure.config.ClientStreamProperties;
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
                (resourceName, resourceIds) -> resourceId -> true,
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
                (resourceName, resourceIds) -> resourceId -> true,
                timeout -> new RecordingEmitter()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> registry.subscribe("claims", List.of())
        );
    }

    @Test
    void shouldRejectSubscriptionWhenOneIdIsNotAuthorized() {
        final ClientStreamProperties properties = new ClientStreamProperties(Duration.ofMinutes(10));
        final ClientStreamRegistry registry = new ClientStreamRegistry(
                properties,
                (resourceName, resourceIds) -> resourceId -> !resourceId.equals("2"),
                timeout -> new RecordingEmitter()
        );

        assertThrows(
                ClientStreamAccessDeniedException.class,
                () -> registry.subscribe("claims", List.of("1", "2"))
        );
    }

    @Test
    void shouldStopSendingWhenAccessIsRevoked() {
        final ClientStreamProperties properties = new ClientStreamProperties(Duration.ofMinutes(10));
        final RecordingEmitter emitter = new RecordingEmitter();
        final boolean[] allowed = {true};
        final ClientStreamRegistry registry = new ClientStreamRegistry(
                properties,
                (resourceName, resourceIds) -> resourceId -> allowed[0],
                timeout -> emitter
        );

        registry.subscribe("claims", List.of("1"));
        allowed[0] = false;
        registry.publish("claims", "1", "claim-1");

        assertEquals(0, emitter.events);
    }

    @Test
    void shouldRejectUnauthorizedResourceUpdate() {
        final ClientStreamProperties properties = new ClientStreamProperties(Duration.ofMinutes(10));
        final ClientStreamRegistry registry = new ClientStreamRegistry(
                properties,
                (resourceName, resourceIds) -> resourceId -> false,
                timeout -> new RecordingEmitter()
        );

        assertThrows(
                ClientStreamAccessDeniedException.class,
                () -> registry.assertAuthorized("claims", "1")
        );
    }

    @Test
    void shouldEnforceSubscriptionLimits() {
        final ClientStreamProperties properties = new ClientStreamProperties(
                Duration.ofMinutes(10),
                1,
                1
        );
        final ClientStreamRegistry registry = new ClientStreamRegistry(
                properties,
                (resourceName, resourceIds) -> resourceId -> true,
                timeout -> new RecordingEmitter()
        );

        assertThrows(
                ClientStreamLimitExceededException.class,
                () -> registry.subscribe("claims", List.of("1", "2"))
        );
        registry.subscribe("claims", List.of("1"));
        assertThrows(
                ClientStreamLimitExceededException.class,
                () -> registry.subscribe("claims", List.of("2"))
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
