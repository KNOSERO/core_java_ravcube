package com.ravcube.lib.event.listener;

import com.ravcube.lib.event.inteface.AbstractListener;
import com.ravcube.lib.event.DomainEvent;
import com.ravcube.lib.event.enums.EventSource;

public abstract class DefaultKafkaCommitListener<E extends DomainEvent> implements AbstractListener<E> {

    @Override
    public final EventSource source() {
        return EventSource.KAFKA_AFTER_COMMIT;
    }
}
