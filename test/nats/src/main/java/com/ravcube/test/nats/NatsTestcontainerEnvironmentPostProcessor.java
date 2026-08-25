package com.ravcube.test.nats;

import com.ravcube.test.common.container.SharedContainer;
import com.ravcube.test.common.env.BaseTestcontainerEnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public final class NatsTestcontainerEnvironmentPostProcessor
        extends BaseTestcontainerEnvironmentPostProcessor<GenericContainer<?>>
        implements NatsTestcontainerConstants {

    private static final int NATS_PORT = 4222;
    private static final SharedContainer<GenericContainer<?>> SHARED_NATS_CONTAINER = new SharedContainer<>();

    @Override
    protected SharedContainer<GenericContainer<?>> sharedContainer() {
        return SHARED_NATS_CONTAINER;
    }

    @Override
    protected String propertySourceName() {
        return PROPERTY_SOURCE_NAME;
    }

    @Override
    protected String profile() {
        return NATS_PROFILE;
    }

    @Override
    protected String enabledProperty() {
        return NATS_ENABLED_PROPERTY;
    }

    @Override
    protected String imageProperty() {
        return NATS_IMAGE_PROPERTY;
    }

    @Override
    protected String defaultImage() {
        return DEFAULT_NATS_IMAGE;
    }

    @Override
    protected String shutdownHookName() {
        return NATS_SHUTDOWN_HOOK_NAME;
    }

    @Override
    protected java.util.function.Predicate<GenericContainer<?>> runningChecker() {
        return container -> container.isRunning();
    }

    @Override
    protected GenericContainer<?> createContainer(String imageName) {
        return new GenericContainer<>(DockerImageName.parse(imageName))
                .withExposedPorts(NATS_PORT)
                .waitingFor(org.testcontainers.containers.wait.strategy.Wait.forListeningPort());
    }

    @Override
    protected Map<String, Object> properties(
            GenericContainer<?> container,
            ConfigurableEnvironment environment
    ) {
        return Map.of(
                NATS_URL_PROPERTY,
                "nats://%s:%d".formatted(container.getHost(), container.getMappedPort(NATS_PORT))
        );
    }
}
