package com.ravcube.lib.event.listener;

import com.ravcube.lib.event.domain.SpringDomainEvent;
import com.ravcube.lib.event.service.listener.DefaultRollbackListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class SpringRollbackListener extends DefaultRollbackListener<SpringDomainEvent> {

    private static final AtomicBoolean SUCCESS = new AtomicBoolean(false);
    private static final AtomicInteger INVOCATIONS = new AtomicInteger();
    private static final AtomicReference<SpringDomainEvent> LAST_EVENT = new AtomicReference<>();

    @Override
    public void on(SpringDomainEvent event) {
        SUCCESS.set(true);
        LAST_EVENT.set(event);
        INVOCATIONS.incrementAndGet();
    }

    public static void reset() {
        SUCCESS.set(false);
        LAST_EVENT.set(null);
        INVOCATIONS.set(0);
    }

    public static boolean success() {
        return SUCCESS.get();
    }

    public static int invocations() {
        return INVOCATIONS.get();
    }

    public static SpringDomainEvent lastEvent() {
        return LAST_EVENT.get();
    }
}
