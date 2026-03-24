package com.ravcube.lib.event.service.kafka;

import com.ravcube.lib.event.enums.EventSource;
import org.springframework.stereotype.Component;

@Component
public class RollbackKafkaListener extends DefaultKafkaListener {

    @Override
    protected EventSource typed() {
        return EventSource.KAFKA_AFTER_ROLLBACK;
    }
}
