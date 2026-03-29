package com.ravcube.test.redis;

import com.ravcube.test.common.container.SharedContainer;
import com.ravcube.test.common.env.BaseEnvironmentPostProcessor;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public final class RedisTestEnvironmentPostProcessor
        extends BaseEnvironmentPostProcessor implements RedisTestcontainerConstants {

    private static final SharedContainer<GenericContainer<?>> SHARED_REDIS_CONTAINER = new SharedContainer<>();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!isProfileActive(environment, application, REDIS_PROFILE)
                || !isEnabled(environment, REDIS_ENABLED_PROPERTY)) {
            return;
        }

        GenericContainer<?> redisContainer = SHARED_REDIS_CONTAINER.start(
                resolveRedisImage(environment),
                this::createRedisContainer,
                GenericContainer::isRunning,
                REDIS_SHUTDOWN_HOOK_NAME
        );
        registerRedisProperties(environment, redisContainer);
    }

    private String resolveRedisImage(ConfigurableEnvironment environment) {
        return environment.getProperty(REDIS_IMAGE_PROPERTY, DEFAULT_REDIS_IMAGE);
    }

    private GenericContainer<?> createRedisContainer(String imageName) {
        return new GenericContainer<>(DockerImageName.parse(imageName))
                .withExposedPorts(REDIS_INTERNAL_PORT);
    }

    private void registerRedisProperties(ConfigurableEnvironment environment, GenericContainer<?> redisContainer) {
        Map<String, Object> redisProperties = Map.of(
                REDIS_HOST_PROPERTY, redisContainer.getHost(),
                REDIS_PORT_PROPERTY, redisContainer.getMappedPort(REDIS_INTERNAL_PORT));

        addFirstPropertySource(environment, PROPERTY_SOURCE_NAME, redisProperties);
    }
}
