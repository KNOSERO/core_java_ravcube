package com.ravcube.lib.event.publisher;

import com.ravcube.lib.event.inteface.AbstractPublisher;
import com.ravcube.lib.event.DomainEvent;
import com.ravcube.lib.event.inteface.EventPublisher;
import com.ravcube.lib.event.StoragePublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ConfigRoutingEventPublisher {

    @Bean
    public EventPublisher eventDispatcher(List<AbstractPublisher<? extends DomainEvent>> publishers) {
        return new RoutingEventPublisher(publishers);
    }

    final class RoutingEventPublisher implements EventPublisher {

        private final StoragePublisher storage;

        RoutingEventPublisher(List<AbstractPublisher<? extends DomainEvent>> publishers) {
            this.storage = StoragePublisher.of(publishers);
        }

        @Override
        public void publish(DomainEvent event) {
            storage.publish(event);
        }

        @Override
        public void publish(List<DomainEvent> event) {
            event.forEach(this::publish);
        }
    }
}
