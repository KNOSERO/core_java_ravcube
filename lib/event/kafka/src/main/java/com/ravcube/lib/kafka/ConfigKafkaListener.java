package com.ravcube.lib.kafka;

import com.ravcube.lib.event.inteface.KafkaEvent;
import com.ravcube.lib.event.inteface.KafkaPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class ConfigKafkaListener {

    @Bean
    <T extends KafkaEvent> KafkaPublisher<T> kafkaPublisher(KafkaTemplate<String, T> kafkaTemplate) {
        return new DefaultKafkaPublisher<>(kafkaTemplate);
    }
}
