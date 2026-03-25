package com.ravcube.lib.event.test;

import com.ravcube.lib.event.config.TestApplication;
import com.ravcube.lib.event.domain.KafkaDomainEvent;
import com.ravcube.lib.event.inteface.EventPublisher;
import com.ravcube.lib.event.listener.KafkaCommitListener;
import com.ravcube.lib.event.listener.KafkaRollbackListener;
import com.ravcube.lib.event.publisher.DefaultKafkaPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ActiveProfiles({"test", "kafka"})
@SpringBootTest(classes = {TestApplication.class, KafkaCommitPublisherContainerTest.KafkaCommitPublisherConfig.class})
class KafkaCommitPublisherContainerTest {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private EventPublisher publisher;

    @BeforeEach
    void beforeEach() {
        KafkaCommitListener.reset();
        KafkaRollbackListener.reset();
    }

    @Test
    void shouldPublishAndConsumeKafkaEventAfterCommit() {
        KafkaDomainEvent event = new KafkaDomainEvent(UUID.randomUUID(), "commit");

        transactionTemplate.executeWithoutResult(status -> publisher.publish(event));

        KafkaDomainEvent consumedEvent = KafkaCommitListener.awaitEvent(Duration.ofSeconds(10));
        assertNotNull(consumedEvent);
        assertEquals(event.id(), consumedEvent.id());
        assertEquals(event.message(), consumedEvent.message());
        assertNull(KafkaRollbackListener.awaitEvent(Duration.ofMillis(500)));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class KafkaCommitPublisherConfig {
        @Bean
        KafkaCommitPublisher kafkaCommitPublisher() {
            return new KafkaCommitPublisher();
        }
    }

    static class KafkaCommitPublisher extends DefaultKafkaPublisher<KafkaDomainEvent> {
    }
}
