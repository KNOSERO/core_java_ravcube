package com.ravcube.lib.event.nats;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

@Configuration
@Profile("nats")
public class NatsConfiguration {

    @Bean
    NatsProperties natsProperties(
            @Value("${ravcube.nats.url:nats://localhost:4222}") String url,
            @Value("${ravcube.nats.subject-prefix:${spring.application.name:application}}") String subjectPrefix
    ) {
        NatsProperties properties = new NatsProperties();
        properties.setUrl(url);
        properties.setSubjectPrefix(subjectPrefix);
        return properties;
    }

    @Bean
    NatsSubjectResolver natsSubjectResolver(NatsProperties properties) {
        return new NatsSubjectResolver(properties);
    }

    @Bean(destroyMethod = "close")
    Connection natsConnection(NatsProperties properties) throws IOException, InterruptedException {
        Options options = new Options.Builder()
                .server(properties.getUrl())
                .maxReconnects(-1)
                .build();
        return Nats.connect(options);
    }
}
