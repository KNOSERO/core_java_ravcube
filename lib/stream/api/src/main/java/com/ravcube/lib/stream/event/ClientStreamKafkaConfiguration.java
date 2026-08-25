package com.ravcube.lib.stream.event;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration(proxyBeanMethods = false)
@Profile("kafka")
final class ClientStreamKafkaConfiguration {

    @Bean(name = "clientStreamKafkaListenerContainerFactory")
    ConcurrentKafkaListenerContainerFactory<Object, Object> clientStreamKafkaListenerContainerFactory(
            @Qualifier("kafkaListenerContainerFactory")
            ConcurrentKafkaListenerContainerFactory<Object, Object> defaultFactory
    ) {
        final ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(defaultFactory.getConsumerFactory());
        factory.setConcurrency(1);
        copyContainerProperties(defaultFactory.getContainerProperties(), factory.getContainerProperties());
        factory.setCommonErrorHandler(
                new DefaultErrorHandler(new FixedBackOff(1_000L, 2L))
        );
        return factory;
    }

    private static void copyContainerProperties(
            ContainerProperties source,
            ContainerProperties target
    ) {
        target.setAckMode(source.getAckMode());
        target.setAckCount(source.getAckCount());
        target.setAckTime(source.getAckTime());
        target.setPollTimeout(source.getPollTimeout());
        target.setSyncCommits(source.isSyncCommits());
        target.setSyncCommitTimeout(source.getSyncCommitTimeout());
        target.setMissingTopicsFatal(source.isMissingTopicsFatal());
        target.setMicrometerEnabled(source.isMicrometerEnabled());
        target.setObservationEnabled(source.isObservationEnabled());
        target.setIdleBetweenPolls(source.getIdleBetweenPolls());
        target.setShutdownTimeout(source.getShutdownTimeout());
        target.setStopImmediate(source.isStopImmediate());
        target.setPauseImmediate(source.isPauseImmediate());
    }
}
