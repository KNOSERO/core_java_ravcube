package com.ravcube.lib.event.listener;

import com.ravcube.lib.event.DomainEvent;
import com.ravcube.lib.event.routing.AbstractEventListener;
import com.ravcube.lib.event.routing.EventSource;

public abstract class DefaultKafkaCommitListener<E extends DomainEvent> implements AbstractEventListener<E> {

    @Override
    public final EventSource source() {
        return EventSource.KAFKA_AFTER_COMMIT;
    }
}
