package com.ravcube.test.redis;

import com.ravcube.test.common.container.SharedContainer;
import com.ravcube.test.common.env.BaseTestcontainerEnvironmentPostProcessor;
import java.util.Map;
import org.springframework.core.env.ConfigurableEnvironment;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public final class RedisTestEnvironmentPostProcessor
        extends BaseTestcontainerEnvironmentPostProcessor<GenericContainer<?>>
        implements RedisTestcontainerConstants {

    private static final SharedContainer<GenericContainer<?>> SHARED_REDIS_CONTAINER = new SharedContainer<>();

    @Override
    protected SharedContainer<GenericContainer<?>> sharedContainer() {
        return SHARED_REDIS_CONTAINER;
    }

    @Override
    protected String propertySourceName() {
        return PROPERTY_SOURCE_NAME;
    }

    @Override
    protected String profile() {
        return REDIS_PROFILE;
    }

    @Override
    protected String enabledProperty() {
        return REDIS_ENABLED_PROPERTY;
    }

    @Override
    protected String imageProperty() {
        return REDIS_IMAGE_PROPERTY;
    }

    @Override
    protected String defaultImage() {
        return DEFAULT_REDIS_IMAGE;
    }

    @Override
    protected String shutdownHookName() {
        return REDIS_SHUTDOWN_HOOK_NAME;
    }

    @Override
    protected java.util.function.Predicate<GenericContainer<?>> runningChecker() {
        return GenericContainer::isRunning;
    }

    @Override
    protected GenericContainer<?> createContainer(String imageName) {
        return new GenericContainer<>(DockerImageName.parse(imageName))
                .withExposedPorts(REDIS_INTERNAL_PORT);
    }

    @Override
    protected Map<String, Object> properties(GenericContainer<?> container, ConfigurableEnvironment environment) {
        return Map.of(
                REDIS_HOST_PROPERTY, container.getHost(),
                REDIS_PORT_PROPERTY, container.getMappedPort(REDIS_INTERNAL_PORT)
        );
    }
}
