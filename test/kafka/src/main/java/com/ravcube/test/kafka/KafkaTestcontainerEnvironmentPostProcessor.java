package com.ravcube.test.kafka;

import com.ravcube.test.common.container.SharedContainer;
import com.ravcube.test.common.env.BaseTestcontainerEnvironmentPostProcessor;
import java.util.Map;
import org.springframework.core.env.ConfigurableEnvironment;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

public final class KafkaTestcontainerEnvironmentPostProcessor
        extends BaseTestcontainerEnvironmentPostProcessor<ConfluentKafkaContainer>
        implements KafkaTestcontainerConstants {

    private static final SharedContainer<ConfluentKafkaContainer> SHARED_KAFKA_CONTAINER = new SharedContainer<>();

    @Override
    protected SharedContainer<ConfluentKafkaContainer> sharedContainer() {
        return SHARED_KAFKA_CONTAINER;
    }

    @Override
    protected String propertySourceName() {
        return PROPERTY_SOURCE_NAME;
    }

    @Override
    protected String profile() {
        return KAFKA_PROFILE;
    }

    @Override
    protected String enabledProperty() {
        return KAFKA_ENABLED_PROPERTY;
    }

    @Override
    protected String imageProperty() {
        return KAFKA_IMAGE_PROPERTY;
    }

    @Override
    protected String defaultImage() {
        return DEFAULT_KAFKA_IMAGE;
    }

    @Override
    protected String shutdownHookName() {
        return KAFKA_SHUTDOWN_HOOK_NAME;
    }

    @Override
    protected java.util.function.Predicate<ConfluentKafkaContainer> runningChecker() {
        return ConfluentKafkaContainer::isRunning;
    }

    @Override
    protected ConfluentKafkaContainer createContainer(String imageName) {
        return new ConfluentKafkaContainer(DockerImageName.parse(imageName));
    }

    @Override
    protected Map<String, Object> properties(ConfluentKafkaContainer container, ConfigurableEnvironment environment) {
        return Map.of(BOOTSTRAP_SERVERS_PROPERTY, container.getBootstrapServers());
    }
}
