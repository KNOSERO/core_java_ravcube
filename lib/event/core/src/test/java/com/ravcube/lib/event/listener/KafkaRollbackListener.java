package com.ravcube.lib.event.listener;

import com.ravcube.lib.event.domain.KafkaDomainEvent;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
public class KafkaRollbackListener extends DefaultKafkaRollbackListener<KafkaDomainEvent> {

    private static final AtomicBoolean SUCCESS = new AtomicBoolean(false);
    private static final AtomicInteger INVOCATIONS = new AtomicInteger();
    private static final ConcurrentMap<UUID, AtomicInteger> INVOCATIONS_BY_EVENT = new ConcurrentHashMap<>();
    private static final AtomicReference<KafkaDomainEvent> LAST_EVENT = new AtomicReference<>();
    private static final BlockingQueue<KafkaDomainEvent> EVENTS = new LinkedBlockingQueue<>();

    @Override
    public void on(KafkaDomainEvent event) {
        SUCCESS.set(true);
        LAST_EVENT.set(event);
        INVOCATIONS.incrementAndGet();
        INVOCATIONS_BY_EVENT.computeIfAbsent(event.id(), key -> new AtomicInteger()).incrementAndGet();
        EVENTS.offer(event);
    }

    public static void reset() {
        SUCCESS.set(false);
        LAST_EVENT.set(null);
        INVOCATIONS.set(0);
        INVOCATIONS_BY_EVENT.clear();
        EVENTS.clear();
    }

    public static KafkaDomainEvent awaitEvent(Duration timeout) {
        try {
            return EVENTS.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for Kafka rollback event", ex);
        }
    }

    public static KafkaDomainEvent awaitEventMatching(UUID eventId, Duration timeout) {
        final long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            KafkaDomainEvent event = awaitEvent(Duration.ofMillis(100));
            if (event == null) {
                continue;
            }
            if (event.id().equals(eventId)) {
                return event;
            }
        }
        return null;
    }

    public static boolean success() {
        return SUCCESS.get();
    }

    public static int invocations() {
        return INVOCATIONS.get();
    }

    public static int invocations(UUID eventId) {
        AtomicInteger counter = INVOCATIONS_BY_EVENT.get(eventId);
        return counter == null ? 0 : counter.get();
    }

    public static KafkaDomainEvent lastEvent() {
        return LAST_EVENT.get();
    }
}
