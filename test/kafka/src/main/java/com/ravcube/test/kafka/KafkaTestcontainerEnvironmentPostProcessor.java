package com.ravcube.test.kafka;

import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public final class KafkaTestcontainerEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "ravcubeTestKafkaContainer";
    private static final String BOOTSTRAP_SERVERS_PROPERTY = "spring.kafka.bootstrap-servers";
    private static final String KAFKA_ENABLED_PROPERTY = "ravcube.testcontainers.kafka.enabled";
    private static final String KAFKA_IMAGE_PROPERTY = "ravcube.testcontainers.kafka.image";
    private static final String DEFAULT_KAFKA_IMAGE = "apache/kafka-native:3.9.0";

    private static final SharedKafkaContainer SHARED_KAFKA_CONTAINER = new SharedKafkaContainer();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!environment.getProperty(KAFKA_ENABLED_PROPERTY, Boolean.class, true)) {
            return;
        }

        if (StringUtils.hasText(environment.getProperty(BOOTSTRAP_SERVERS_PROPERTY))) {
            return;
        }

        String imageName = environment.getProperty(KAFKA_IMAGE_PROPERTY, DEFAULT_KAFKA_IMAGE);
        KafkaContainer kafkaContainer = SHARED_KAFKA_CONTAINER.start(imageName);
        environment.getPropertySources()
                .addFirst(new MapPropertySource(
                        PROPERTY_SOURCE_NAME,
                        Map.of(BOOTSTRAP_SERVERS_PROPERTY, kafkaContainer.getBootstrapServers())));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private static final class SharedKafkaContainer {
        private KafkaContainer kafkaContainer;

        synchronized KafkaContainer start(String imageName) {
            if (kafkaContainer == null) {
                kafkaContainer = new KafkaContainer(DockerImageName.parse(imageName));
                kafkaContainer.start();
                Runtime.getRuntime().addShutdownHook(new Thread(kafkaContainer::stop, "ravcube-test-kafka-stop"));
            }

            return kafkaContainer;
        }
    }
}
