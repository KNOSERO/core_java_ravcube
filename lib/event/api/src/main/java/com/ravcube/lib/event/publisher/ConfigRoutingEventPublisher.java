package com.ravcube.lib.event.publisher;

import com.ravcube.lib.event.DomainEvent;
import com.ravcube.lib.event.api.EventPublisher;
import com.ravcube.lib.event.routing.AbstractEventPublisher;
import com.ravcube.lib.event.routing.EventPublisherRegistry;
import com.ravcube.lib.event.listener.ConfigRoutingEventListener;
import com.ravcube.lib.logger.core.LoggerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;

@Configuration
@Import({
        ConfigRoutingEventListener.class,
        LoggerConfiguration.class
})
public class ConfigRoutingEventPublisher {

    @Bean
    public EventPublisher eventPublisher(
            List<AbstractEventPublisher<? extends DomainEvent>> publishers
    ) {
        return new RoutingEventPublisher(publishers);
    }

    final class RoutingEventPublisher implements EventPublisher {

        private final EventPublisherRegistry publishers;

        RoutingEventPublisher(List<AbstractEventPublisher<? extends DomainEvent>> publishers) {
            this.publishers = EventPublisherRegistry.of(publishers);
        }

        @Override
        public void publish(DomainEvent event) {
            publishers.publish(event);
        }

        @Override
        public void publish(List<? extends DomainEvent> events) {
            events.forEach(this::publish);
        }
    }
}
