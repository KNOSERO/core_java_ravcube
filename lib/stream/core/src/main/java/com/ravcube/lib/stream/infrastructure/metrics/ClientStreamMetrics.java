package com.ravcube.lib.stream.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;

import java.util.concurrent.atomic.AtomicInteger;

public final class ClientStreamMetrics {

    private final AtomicInteger activeSubscriptions = new AtomicInteger();
    private final Counter rejectedSubscriptions;
    private final Counter queueOverflows;
    private final Counter sendFailures;
    private final Counter heartbeatFailures;

    private ClientStreamMetrics(MeterRegistry registry) {
        if (registry == null) {
            rejectedSubscriptions = null;
            queueOverflows = null;
            sendFailures = null;
            heartbeatFailures = null;
            return;
        }

        rejectedSubscriptions = Counter.builder("ravcube.stream.subscriptions.rejected")
                .description("Stream subscriptions rejected by capacity limits")
                .register(registry);
        queueOverflows = Counter.builder("ravcube.stream.events.queue.overflow")
                .description("Stream subscriptions removed because their pending queue was full")
                .register(registry);
        sendFailures = Counter.builder("ravcube.stream.events.send.failure")
                .description("Stream event sends that failed")
                .register(registry);
        heartbeatFailures = Counter.builder("ravcube.stream.heartbeat.failure")
                .description("Stream heartbeat sends that failed")
                .register(registry);
        registry.gauge(
                "ravcube.stream.subscriptions.active",
                activeSubscriptions,
                AtomicInteger::get
        );
    }

    public static ClientStreamMetrics from(ObjectProvider<MeterRegistry> registries) {
        return new ClientStreamMetrics(registries.getIfAvailable());
    }

    public static ClientStreamMetrics noop() {
        return new ClientStreamMetrics(null);
    }

    public void subscriptionOpened() {
        activeSubscriptions.incrementAndGet();
    }

    public void subscriptionClosed() {
        activeSubscriptions.updateAndGet(value -> Math.max(0, value - 1));
    }

    public void subscriptionRejected() {
        increment(rejectedSubscriptions);
    }

    public void queueOverflow() {
        increment(queueOverflows);
    }

    public void sendFailure() {
        increment(sendFailures);
    }

    public void heartbeatFailure() {
        increment(heartbeatFailures);
    }

    private static void increment(Counter counter) {
        if (counter != null) {
            counter.increment();
        }
    }
}
