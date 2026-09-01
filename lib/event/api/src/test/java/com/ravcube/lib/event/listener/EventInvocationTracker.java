package com.ravcube.lib.event.listener;

import com.ravcube.lib.event.domain.EventExecutionId;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public final class EventInvocationTracker<E, K> {

    private static final Duration DEFAULT_INVOCATIONS_DELAY = Duration.ofMillis(200);
    private static final long INVOCATIONS_POLL_NANOS = TimeUnit.MILLISECONDS.toNanos(10L);

    private final Function<E, K> eventIdResolver;
    private final ConcurrentMap<EventExecutionId<K>, AtomicInteger> invocationsByEvent = new ConcurrentHashMap<>();

    private EventInvocationTracker(Function<E, K> eventIdResolver) {
        this.eventIdResolver = Objects.requireNonNull(eventIdResolver, "eventIdResolver cannot be null");
    }

    public static <E, K> EventInvocationTracker<E, K> of(Function<E, K> eventIdResolver) {
        return new EventInvocationTracker<>(eventIdResolver);
    }

    public void register(E event) {
        E safeEvent = Objects.requireNonNull(event, "event cannot be null");
        EventExecutionId<K> eventId = eventIdOf(safeEvent);

        invocationsByEvent.computeIfAbsent(eventId, key -> new AtomicInteger()).incrementAndGet();
    }

    public void reset() {
        invocationsByEvent.clear();
    }

    public int invocations(K eventId) {
        return invocations(eventId, DEFAULT_INVOCATIONS_DELAY);
    }

    public int invocations(K eventId, Duration delay) {
        Objects.requireNonNull(delay, "delay cannot be null");
        EventExecutionId<K> resolvedEventId = EventExecutionId.of(eventId);
        long deadlineNanos = deadlineNanos(delay);
        return awaitInvocations(resolvedEventId, deadlineNanos);
    }

    private EventExecutionId<K> eventIdOf(E event) {
        return EventExecutionId.of(
                Objects.requireNonNull(eventIdResolver.apply(event), "resolved event id cannot be null")
        );
    }

    private int currentInvocations(EventExecutionId<K> eventId) {
        AtomicInteger counter = invocationsByEvent.get(eventId);
        return counter == null ? 0 : counter.get();
    }

    private int awaitInvocations(EventExecutionId<K> eventId, long deadlineNanos) {
        while (true) {
            int invocations = currentInvocations(eventId);
            if (invocations > 0) {
                return invocations;
            }

            long remainingNanos = remainingNanos(deadlineNanos);
            if (remainingNanos <= 0L) {
                return currentInvocations(eventId);
            }
            pause(pauseNanos(remainingNanos));
        }
    }

    private long deadlineNanos(Duration delay) {
        return System.nanoTime() + Math.max(0L, delay.toNanos());
    }

    private long pauseNanos(long remainingNanos) {
        return Math.min(INVOCATIONS_POLL_NANOS, remainingNanos);
    }

    private long remainingNanos(long deadlineNanos) {
        return deadlineNanos - System.nanoTime();
    }

    private void pause(long nanos) {
        try {
            TimeUnit.NANOSECONDS.sleep(nanos);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for invocations", ex);
        }
    }
}
