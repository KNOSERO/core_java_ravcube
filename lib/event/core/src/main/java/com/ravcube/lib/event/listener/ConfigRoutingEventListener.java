package com.ravcube.lib.event.listener;

import com.ravcube.lib.event.inteface.AbstractListener;
import com.ravcube.lib.event.DomainEvent;
import com.ravcube.lib.event.inteface.EventListener;
import com.ravcube.lib.event.enums.EventSource;

import com.ravcube.lib.event.StorageListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ConfigRoutingEventListener {

    @Bean
    public EventListener routingEventListener(List<AbstractListener<? extends DomainEvent>> listeners) {
        return new RoutingEventListener(listeners);
    }

    final class RoutingEventListener implements EventListener {
        private final StorageListener storage;

        RoutingEventListener(List<AbstractListener<? extends DomainEvent>> listeners) {
            this.storage = StorageListener.of(listeners);
        }

        @Override
        public void on(EventSource source, DomainEvent event) {
            storage.on(source, event);
        }

        @Override
        public List<String> getTopics(EventSource source) {
            return storage.getTopics(source);
        }
    }
}
