package com.ravcube.lib.event.inteface;

import com.ravcube.lib.event.DomainEvent;

import java.util.Map;

public interface KafkaPublisher<T extends DomainEvent> {

    void publish(T evant);

    void publish(T evant, Map<String, byte[]> headers);
}
