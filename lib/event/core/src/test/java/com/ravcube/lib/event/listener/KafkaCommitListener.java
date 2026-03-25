package com.ravcube.lib.event.listener;


import com.ravcube.lib.event.domain.KafkaDomainEvent;
import org.springframework.stereotype.Component;

@Component
public class KafkaCommitListener extends DefaultKafkaCommitListener<KafkaDomainEvent> {

    @Override
    public void on(KafkaDomainEvent event) {

    }
}
