package com.ravcube.lib.event.kafka;

import com.ravcube.lib.event.enums.EventSource;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("kafka")
public class CommitKafkaListener extends DefaultKafkaListener {

    @Override
    protected EventSource typed() {
        return EventSource.KAFKA_AFTER_COMMIT;
    }
}
