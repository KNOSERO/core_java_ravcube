package com.ravcube.lib.event.listener;

import com.ravcube.lib.event.DomainEvent;
import com.ravcube.lib.event.routing.AbstractEventListener;
import com.ravcube.lib.event.routing.EventListenerRegistry;
import com.ravcube.lib.event.routing.EventRouter;
import com.ravcube.lib.event.routing.EventSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ConfigRoutingEventListener {

    @Bean
    public EventRouter eventRouter(List<AbstractEventListener<? extends DomainEvent>> listeners) {
        return new RoutingEventListener(listeners);
    }

    final class RoutingEventListener implements EventRouter {
        private final EventListenerRegistry listeners;

        RoutingEventListener(List<AbstractEventListener<? extends DomainEvent>> listeners) {
            this.listeners = EventListenerRegistry.of(listeners);
        }

        @Override
        public void on(EventSource source, DomainEvent event) {
            listeners.on(source, event);
        }

        @Override
        public List<String> topics(EventSource source) {
            return listeners.topics(source);
        }
    }
}
