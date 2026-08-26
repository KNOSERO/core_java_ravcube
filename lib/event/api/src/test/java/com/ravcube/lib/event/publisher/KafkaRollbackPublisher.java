package com.ravcube.lib.event.publisher;

import com.ravcube.lib.event.domain.KafkaDomainEvent;
import org.springframework.stereotype.Component;

@Component
public class KafkaRollbackPublisher extends DefaultKafkaRollbackPublisher<KafkaDomainEvent> {
}
