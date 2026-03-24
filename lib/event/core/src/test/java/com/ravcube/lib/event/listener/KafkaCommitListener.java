package com.ravcube.lib.event.listener;


import com.ravcube.lib.event.domain.KafkaDomainEvent;
import com.ravcube.lib.event.service.listener.DefaultKafkaCommitListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaCommitListener extends DefaultKafkaCommitListener<KafkaDomainEvent> {

    @Override
    public void on(KafkaDomainEvent event) {

    }
}
