package com.ravcube.lib.event.listener;

import com.ravcube.lib.event.domain.SpringDomainEvent;
import com.ravcube.lib.event.service.listener.DefaultCommitListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SpringCommitAuditListener extends DefaultCommitListener<SpringDomainEvent> {

    private static final AtomicInteger INVOCATIONS = new AtomicInteger();

    @Override
    public void on(SpringDomainEvent event) {
        INVOCATIONS.incrementAndGet();
    }

    public static void reset() {
        INVOCATIONS.set(0);
    }

    public static int invocations() {
        return INVOCATIONS.get();
    }
}
