package com.ravcube.lib.stream.infrastructure.sse;

import com.ravcube.lib.stream.common.ClientStreamCapacityExceededException;
import com.ravcube.lib.stream.infrastructure.config.ClientStreamProperties;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.ravcube.lib.stream.infrastructure.sse.ClientStreamRegistryTestSupport.RecordingSseEmitter;
import static com.ravcube.lib.stream.infrastructure.sse.ClientStreamRegistryTestSupport.registry;
import static org.awaitility.Awaitility.await;
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
    void unsubscribedClientReceivesNothing() {
        final RecordingSseEmitter subscriber = new RecordingSseEmitter();
        final ClientStreamRegistry registry = registry(subscriber);

        final SseEmitter connection = registry.subscribe("claims", List.of("1"));
        registry.unsubscribe(connection);
        registry.publish("claims", "1", 42);

        assertEquals(0, subscriber.eventCount());
        assertEquals(0, registry.activeSubscriptions());
    }

    @Test
    void slowClientIsRemovedWhenPendingQueueIsFull() {
        final RecordingSseEmitter subscriber = new RecordingSseEmitter();
        final ClientStreamProperties properties = new ClientStreamProperties(
                Duration.ofMinutes(10),
                10,
                10,
                10,
                1,
                Duration.ofMinutes(1)
        );
        final ClientStreamRegistry registry = new ClientStreamRegistry(
                properties,
                timeout -> subscriber,
                ignored -> {
                }
        );

        registry.subscribe("claims", List.of("1"));
        registry.publish("claims", "1", 1);
        registry.publish("claims", "1", 2);

        assertEquals(0, registry.activeSubscriptions());
    }

    @Test
    void heartbeatIsSentForActiveSubscription() {
        final RecordingSseEmitter subscriber = new RecordingSseEmitter();
        final ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();
        final ClientStreamProperties properties = new ClientStreamProperties(
                Duration.ofMinutes(10),
                10,
                10,
                10,
                100,
                Duration.ofMillis(10)
        );
        final ClientStreamRegistry registry = new ClientStreamRegistry(
                properties,
                timeout -> subscriber,
                Runnable::run,
                scheduler
        );

        try {
            registry.subscribe("claims", List.of("1"));

            await()
                    .atMost(Duration.ofSeconds(1))
                    .until(() -> subscriber.eventCount() > 0);
        } finally {
            registry.destroy();
            scheduler.shutdownNow();
        }
    }

    @Test
    void idAndGlobalSubscriptionLimitsUseCapacityException() {
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
                ClientStreamCapacityExceededException.class,
                () -> registry.subscribe("claims", List.of("1", "2"))
        );
        registry.subscribe("claims", List.of("1"));
        assertThrows(
                ClientStreamCapacityExceededException.class,
                () -> registry.subscribe("claims", List.of("2"))
        );
    }

    @Test
    void clientSubscriptionLimitIsEnforcedIndependently() {
        final ClientStreamProperties properties = new ClientStreamProperties(
                Duration.ofMinutes(10),
                10,
                10,
                1,
                100,
                Duration.ofMinutes(1)
        );
        final ClientStreamRegistry registry = new ClientStreamRegistry(
                properties,
                timeout -> new RecordingSseEmitter()
        );

        registry.subscribe("claims", List.of("1"), "client-a");

        assertThrows(
                ClientStreamCapacityExceededException.class,
                () -> registry.subscribe("claims", List.of("2"), "client-a")
        );
        registry.subscribe("claims", List.of("2"), "client-b");
    }
}
