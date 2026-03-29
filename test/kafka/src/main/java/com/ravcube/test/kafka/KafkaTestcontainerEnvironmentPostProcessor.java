package com.ravcube.test.kafka;

import com.ravcube.test.common.container.SharedContainer;
import com.ravcube.test.common.env.BaseEnvironmentPostProcessor;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

public final class KafkaTestcontainerEnvironmentPostProcessor extends BaseEnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "ravcubeTestKafkaContainer";
    private static final String BOOTSTRAP_SERVERS_PROPERTY = "spring.kafka.bootstrap-servers";
    private static final String KAFKA_ENABLED_PROPERTY = "ravcube.testcontainers.kafka.enabled";
    private static final String KAFKA_IMAGE_PROPERTY = "ravcube.testcontainers.kafka.image";
    private static final String DEFAULT_KAFKA_IMAGE = "confluentinc/cp-kafka:7.7.0";

    private static final SharedContainer<ConfluentKafkaContainer> SHARED_KAFKA_CONTAINER = new SharedContainer<>();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!environment.getProperty(KAFKA_ENABLED_PROPERTY, Boolean.class, true)) {
            return;
        }

        String imageName = environment.getProperty(KAFKA_IMAGE_PROPERTY, DEFAULT_KAFKA_IMAGE);
        ConfluentKafkaContainer kafkaContainer = SHARED_KAFKA_CONTAINER.start(
                imageName,
                this::createKafkaContainer,
                ConfluentKafkaContainer::isRunning,
                "ravcube-test-kafka-stop"
        );
        addFirstPropertySource(environment, PROPERTY_SOURCE_NAME,
                Map.of(BOOTSTRAP_SERVERS_PROPERTY, kafkaContainer.getBootstrapServers()));
    }

    private ConfluentKafkaContainer createKafkaContainer(String imageName) {
        return new ConfluentKafkaContainer(DockerImageName.parse(imageName));
    }
}
