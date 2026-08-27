package com.ravcube.lib.event.config;

import com.ravcube.lib.event.kafka.CommitKafkaListener;
import com.ravcube.lib.event.kafka.RollbackKafkaListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({
        CommitKafkaListener.class,
        RollbackKafkaListener.class
})
public class ConfigKafkaEventListeners {
}
