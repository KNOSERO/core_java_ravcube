package com.ravcube.lib.stream.infrastructure.sse;

import com.ravcube.lib.stream.application.ClientStreamAccessDeniedException;
import com.ravcube.lib.stream.application.ClientStreamLimitExceededException;
import com.ravcube.lib.stream.infrastructure.config.ClientStreamProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ravcube.lib.stream.infrastructure.sse.ClientStreamRegistryTestSupport.RecordingSseEmitter;
import static com.ravcube.lib.stream.infrastructure.sse.ClientStreamRegistryTestSupport.registry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClientStreamRegistryTest {

    @Test
    void subscriberReceivesUpdateForSubscribedId() {
        final RecordingSseEmitter subscriber = new RecordingSseEmitter();
        final ClientStreamRegistry registry = registry((resourceName, resourceId) -> true, subscriber);

        registry.subscribe("claims", List.of("1"));
        registry.publish("claims", "1", "claim-1");

        assertEquals(1, subscriber.eventCount());
    }

    @Test
    void subscriberReceivesNothingForAnotherId() {
        final RecordingSseEmitter subscriber = new RecordingSseEmitter();
        final ClientStreamRegistry registry = registry((resourceName, resourceId) -> true, subscriber);

        registry.subscribe("claims", List.of("1"));
        registry.publish("claims", "2", "claim-2");

        assertEquals(0, subscriber.eventCount());
    }

    @Test
    void selectedSubscriberReceivesOnlyMatchingUpdates() {
        final RecordingSseEmitter first = new RecordingSseEmitter();
        final RecordingSseEmitter firstAndSecond = new RecordingSseEmitter();
        final RecordingSseEmitter second = new RecordingSseEmitter();
        final ClientStreamRegistry registry = registry(
                (resourceName, resourceId) -> true,
                first,
                firstAndSecond,
                second
        );

        registry.subscribe("claims", List.of("1"));
        registry.subscribe("claims", List.of("1", "2"));
        registry.subscribe("claims", List.of("2"));
        registry.publish("claims", "1", "claim-1");

        assertEquals(1, first.eventCount());
        assertEquals(1, firstAndSecond.eventCount());
        assertEquals(0, second.eventCount());
    }

    @Test
    void subscriptionRequiresAnId() {
        final ClientStreamRegistry registry = registry(
                (resourceName, resourceId) -> true,
                new RecordingSseEmitter()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> registry.subscribe("claims", List.of())
        );
    }

    @Test
    void unauthorizedSubscriptionIsRejected() {
        final ClientStreamRegistry registry = registry(
                (resourceName, resourceId) -> !resourceId.equals("2"),
                new RecordingSseEmitter()
        );

        assertThrows(
                ClientStreamAccessDeniedException.class,
                () -> registry.subscribe("claims", List.of("1", "2"))
        );
    }

    @Test
    void revokedAccessStopsUpdates() {
        final RecordingSseEmitter subscriber = new RecordingSseEmitter();
        final AtomicBoolean allowed = new AtomicBoolean(true);
        final ClientStreamRegistry registry = registry(
                (resourceName, resourceId) -> allowed.get(),
                subscriber
        );

        registry.subscribe("claims", List.of("1"));
        allowed.set(false);
        registry.publish("claims", "1", "claim-1");

        assertEquals(0, subscriber.eventCount());
    }

    @Test
    void subscriptionLimitsAreEnforced() {
        final ClientStreamProperties properties = new ClientStreamProperties(
                Duration.ofMinutes(10),
                1,
                1
        );
        final ClientStreamRegistry registry = new ClientStreamRegistry(
                properties,
                (resourceName, resourceId) -> true,
                timeout -> new RecordingSseEmitter()
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
}
