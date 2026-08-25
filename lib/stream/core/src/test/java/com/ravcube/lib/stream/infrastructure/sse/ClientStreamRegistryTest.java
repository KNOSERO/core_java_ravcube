package com.ravcube.lib.stream.infrastructure.sse;

import com.ravcube.lib.stream.infrastructure.config.ClientStreamProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static com.ravcube.lib.stream.infrastructure.sse.ClientStreamRegistryTestSupport.RecordingSseEmitter;
import static com.ravcube.lib.stream.infrastructure.sse.ClientStreamRegistryTestSupport.registry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClientStreamRegistryTest {

    @Test
    void subscriberReceivesUpdateForSubscribedId() {
        final RecordingSseEmitter subscriber = new RecordingSseEmitter();
        final ClientStreamRegistry registry = registry(subscriber);

        registry.subscribe("claims", List.of("1"));
        registry.publish("claims", "1", 42);

        assertEquals(1, subscriber.eventCount());
    }

    @Test
    void subscriberReceivesNothingForAnotherId() {
        final RecordingSseEmitter subscriber = new RecordingSseEmitter();
        final ClientStreamRegistry registry = registry(subscriber);

        registry.subscribe("claims", List.of("1"));
        registry.publish("claims", "2", 42);

        assertEquals(0, subscriber.eventCount());
    }

    @Test
    void selectedSubscriberReceivesOnlyMatchingUpdates() {
        final RecordingSseEmitter first = new RecordingSseEmitter();
        final RecordingSseEmitter firstAndSecond = new RecordingSseEmitter();
        final RecordingSseEmitter second = new RecordingSseEmitter();
        final ClientStreamRegistry registry = registry(first, firstAndSecond, second);

        registry.subscribe("claims", List.of("1"));
        registry.subscribe("claims", List.of("1", "2"));
        registry.subscribe("claims", List.of("2"));
        registry.publish("claims", "1", 42);

        assertEquals(1, first.eventCount());
        assertEquals(1, firstAndSecond.eventCount());
        assertEquals(0, second.eventCount());
    }

    @Test
    void subscriptionRequiresAnId() {
        final ClientStreamRegistry registry = registry(new RecordingSseEmitter());

        assertThrows(
                IllegalArgumentException.class,
                () -> registry.subscribe("claims", List.of())
        );
    }

    @Test
    void olderVersionIsIgnoredAfterNewerVersion() {
        final RecordingSseEmitter subscriber = new RecordingSseEmitter();
        final ClientStreamRegistry registry = registry(subscriber);

        registry.subscribe("claims", List.of("1"));
        registry.publish("claims", "1", 42);
        registry.publish("claims", "1", 41);

        assertEquals(1, subscriber.eventCount());
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
