package com.ravcube.lib.event.listener;


import com.ravcube.lib.event.domain.KafkaDomainEvent;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
public class KafkaCommitListener extends DefaultKafkaCommitListener<KafkaDomainEvent> {

    private static final BlockingQueue<KafkaDomainEvent> EVENTS = new LinkedBlockingQueue<>();

    @Override
    public void on(KafkaDomainEvent event) {
        EVENTS.offer(event);
    }

    public static void reset() {
        EVENTS.clear();
    }

    public static KafkaDomainEvent awaitEvent(Duration timeout) {
        try {
            return EVENTS.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for Kafka commit event", ex);
        }
    }
}
