package com.ravcube.lib.stream.test.support;

import com.ravcube.lib.stream.api.ClientStreamAuthorization;
import com.ravcube.lib.stream.api.ClientStreamResourceReader;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = {
        "com.ravcube.lib.stream",
        "com.ravcube.lib.event"
})
public class ClientStreamApiTestApplication {

    @Bean
    @Primary
    ClientStreamAuthorization authorization() {
        return (resourceName, resourceId) -> true;
    }

    @Bean
    ClientStreamResourceReader<String> claims() {
        return new ClientStreamResourceReader<>() {
            @Override
            public String resourceName() {
                return "claims";
            }

            @Override
            public String resource(String resourceId) {
                return "claim:" + resourceId;
            }
        };
    }

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
