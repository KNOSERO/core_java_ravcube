package com.ravcube.lib.event.listener;

import com.ravcube.lib.event.domain.KafkaDomainEvent;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class KafkaCommitAuditListener extends DefaultKafkaCommitListener<KafkaDomainEvent> {

    private static final AtomicInteger INVOCATIONS = new AtomicInteger();
    private static final ConcurrentMap<UUID, AtomicInteger> INVOCATIONS_BY_EVENT = new ConcurrentHashMap<>();
    private static final BlockingQueue<KafkaDomainEvent> EVENTS = new LinkedBlockingQueue<>();

    @Override
    public void on(KafkaDomainEvent event) {
        INVOCATIONS.incrementAndGet();
        INVOCATIONS_BY_EVENT.computeIfAbsent(event.id(), key -> new AtomicInteger()).incrementAndGet();
        EVENTS.offer(event);
    }

    public static void reset() {
        INVOCATIONS.set(0);
        INVOCATIONS_BY_EVENT.clear();
        EVENTS.clear();
    }

    public static int invocations() {
        return INVOCATIONS.get();
    }

    public static int invocations(UUID eventId) {
        AtomicInteger counter = INVOCATIONS_BY_EVENT.get(eventId);
        return counter == null ? 0 : counter.get();
    }

    public static KafkaDomainEvent awaitEvent(Duration timeout) {
        try {
            return EVENTS.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for Kafka commit audit event", ex);
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
}
