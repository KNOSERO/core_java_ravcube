package com.ravcube.lib.event.kafka;

import com.ravcube.lib.event.DomainEvent;
import com.ravcube.lib.event.routing.EventRouter;
import com.ravcube.lib.event.routing.EventSource;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.Objects;

public abstract class DefaultKafkaListener {

    private EventRouter eventRouter;

    @KafkaListener(
            topics = "#{__listener.topics()}"
    )
    public final void listen(ConsumerRecord<String, DomainEvent> record) {
        Objects.requireNonNull(record, "record must not be null");
        final DomainEvent payload = Objects.requireNonNull(record.value(), "record value must not be null");

        eventRouter.on(typed(), payload);
    }

    public String[] topics() {
        final EventSource typed = typed();
        return eventRouter.topics(typed).stream()
                .map(typed::formatTopic)
                .toArray(String[]::new);
    }

    protected abstract EventSource typed();

    @Autowired
    public void setEventRouter(EventRouter eventRouter) {
        this.eventRouter = eventRouter;
    }
}
