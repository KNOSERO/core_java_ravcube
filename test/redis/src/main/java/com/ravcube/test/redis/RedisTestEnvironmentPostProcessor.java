package com.ravcube.test.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public final class RedisTestEnvironmentPostProcessor
        implements EnvironmentPostProcessor, Ordered, RedisTestcontainerConstants {

    private static final SharedRedisContainer SHARED_REDIS_CONTAINER = new SharedRedisContainer();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!shouldStartRedisContainer(environment, application)) {
            return;
        }

        GenericContainer<?> redisContainer = SHARED_REDIS_CONTAINER.start(resolveRedisImage(environment));
        registerRedisProperties(environment, redisContainer);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private static final class SharedRedisContainer {
        private GenericContainer<?> redisContainer;

        synchronized GenericContainer<?> start(String imageName) {
            if (redisContainer == null || !redisContainer.isRunning()) {
                GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(imageName))
                        .withExposedPorts(REDIS_INTERNAL_PORT);
                container.start();
                Runtime.getRuntime().addShutdownHook(new Thread(container::stop, REDIS_SHUTDOWN_HOOK_NAME));
                redisContainer = container;
            }

            return redisContainer;
        }
    }

    private boolean shouldStartRedisContainer(ConfigurableEnvironment environment, SpringApplication application) {
        return isRedisProfileActive(environment, application) && isRedisEnabled(environment);
    }

    private boolean isRedisProfileActive(ConfigurableEnvironment environment, SpringApplication application) {
        return environment.acceptsProfiles(REDIS_TEST_PROFILE)
                || application.getAdditionalProfiles().contains(REDIS_PROFILE);
    }

    private boolean isRedisEnabled(ConfigurableEnvironment environment) {
        return environment.getProperty(REDIS_ENABLED_PROPERTY, Boolean.class, true);
    }

    private String resolveRedisImage(ConfigurableEnvironment environment) {
        return environment.getProperty(REDIS_IMAGE_PROPERTY, DEFAULT_REDIS_IMAGE);
    }

    private void registerRedisProperties(ConfigurableEnvironment environment, GenericContainer<?> redisContainer) {
        Map<String, Object> redisProperties = Map.of(
                REDIS_HOST_PROPERTY, redisContainer.getHost(),
                REDIS_PORT_PROPERTY, redisContainer.getMappedPort(REDIS_INTERNAL_PORT));

        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, redisProperties));
    }
}
