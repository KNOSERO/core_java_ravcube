package com.ravcube.lib.event.config;

import com.ravcube.lib.event.listener.KafkaCommitAuditListener;
import com.ravcube.lib.event.listener.KafkaCommitListener;
import com.ravcube.lib.event.listener.KafkaRollbackListener;
import com.ravcube.lib.event.listener.SpringCommitAuditListener;
import com.ravcube.lib.event.listener.SpringCommitListener;
import com.ravcube.lib.event.listener.SpringRollbackListener;
import com.ravcube.lib.event.publisher.ConfigRoutingEventPublisher;
import com.ravcube.lib.event.publisher.KafkaCommitPublisher;
import com.ravcube.lib.event.publisher.KafkaRollbackPublisher;
import com.ravcube.lib.event.publisher.SpringCommitPublisher;
import com.ravcube.lib.event.publisher.SpringRollbackPublisher;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import({
        ConfigRoutingEventPublisher.class,
        KafkaCommitAuditListener.class,
        KafkaCommitListener.class,
        KafkaRollbackListener.class,
        SpringCommitAuditListener.class,
        SpringCommitListener.class,
        SpringRollbackListener.class,
        KafkaCommitPublisher.class,
        KafkaRollbackPublisher.class,
        SpringCommitPublisher.class,
        SpringRollbackPublisher.class
})
public class TestApplication {

    @Bean
    PlatformTransactionManager transactionManager() {
        return new AbstractPlatformTransactionManager() {
            @Override
            protected Object doGetTransaction() {
                return new Object();
            }

            @Override
            protected void doBegin(Object transaction, TransactionDefinition definition) {
            }

            @Override
            protected void doCommit(DefaultTransactionStatus status) {
            }

            @Override
            protected void doRollback(DefaultTransactionStatus status) {
            }
        };
    }

    @Bean
    TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}
