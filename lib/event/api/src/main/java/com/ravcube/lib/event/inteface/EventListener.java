package com.ravcube.lib.event.inteface;

import com.ravcube.lib.event.DomainEvent;
import com.ravcube.lib.event.enums.EventSource;

import java.util.List;

public interface EventListener {

    void on(EventSource source, DomainEvent event);

    List<String> getTopics(EventSource source);
}
