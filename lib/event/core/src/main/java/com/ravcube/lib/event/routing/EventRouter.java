package com.ravcube.lib.event.routing;

import com.ravcube.lib.event.DomainEvent;

import java.util.List;

public interface EventRouter {

    void on(EventSource source, DomainEvent event);

    List<String> topics(EventSource source);
}
