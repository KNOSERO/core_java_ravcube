package com.ravcube.lib.event.listener;

import com.ravcube.lib.event.DomainEvent;
import com.ravcube.lib.event.enums.EventSource;
import com.ravcube.lib.event.inteface.AbstractListener;

public abstract class DefaultNatsCommitListener<E extends DomainEvent> implements AbstractListener<E> {

    @Override
    public final EventSource source() {
        return EventSource.NATS_BROADCAST;
    }
}
