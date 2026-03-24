package com.ravcube.lib.event.service.kafka;

import com.ravcube.lib.event.enums.EventSource;
import org.springframework.stereotype.Component;

@Component
public class CommitKafkaListener extends DefaultKafkaListener {

    @Override
    protected EventSource typed() {
        return EventSource.KAFKA_AFTER_COMMIT;
    }
}
