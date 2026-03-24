package com.ravcube.lib.event.listener;

import com.ravcube.lib.event.domain.KafkaDomainEvent;
import com.ravcube.lib.event.service.listener.DefaultKafkaRollbackListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaRollbackListener extends DefaultKafkaRollbackListener<KafkaDomainEvent> {

    @Override
    public void on(KafkaDomainEvent event) {

    }
}