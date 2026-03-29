package com.ravcube.test.eureka;

import com.ravcube.test.common.container.SharedContainerCluster;
import com.ravcube.test.common.env.BaseEnvironmentPostProcessor;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public final class EurekaTestEnvironmentPostProcessor
        extends BaseEnvironmentPostProcessor implements EurekaTestcontainerConstants {

    private static final SharedContainerCluster<GenericContainer<?>> SHARED_EUREKA_CLUSTER =
            new SharedContainerCluster<>();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!shouldStartEurekaContainer(environment, application)) {
            return;
        }

        List<GenericContainer<?>> eurekaContainers = SHARED_EUREKA_CLUSTER.start(
                resolveEurekaImage(environment),
                resolveEurekaServerCount(environment),
                this::createEurekaContainer,
                GenericContainer::isRunning,
                index -> EUREKA_SHUTDOWN_HOOK_NAME_PREFIX + index
        );
        registerEurekaProperties(environment, eurekaContainers);
    }

    private boolean shouldStartEurekaContainer(ConfigurableEnvironment environment, SpringApplication application) {
        return isProfileActive(environment, application, EUREKA_PROFILE)
                && isEnabled(environment, EUREKA_ENABLED_PROPERTY)
                && isDefaultZoneMissing(environment);
    }

    private boolean isDefaultZoneMissing(ConfigurableEnvironment environment) {
        String configuredDefaultZone = environment.getProperty(EUREKA_SERVICE_URL_PROPERTY);
        if (configuredDefaultZone == null || configuredDefaultZone.isBlank()) {
            return true;
        }

        return DEFAULT_EUREKA_SERVICE_URL.equals(configuredDefaultZone.trim());
    }

    private String resolveEurekaImage(ConfigurableEnvironment environment) {
        return environment.getProperty(EUREKA_IMAGE_PROPERTY, DEFAULT_EUREKA_IMAGE);
    }

    private GenericContainer<?> createEurekaContainer(String imageName) {
        return new GenericContainer<>(DockerImageName.parse(imageName))
                .withExposedPorts(EUREKA_INTERNAL_PORT)
                .waitingFor(Wait.forHttp("/").forPort(EUREKA_INTERNAL_PORT))
                .withStartupTimeout(Duration.ofMinutes(2));
    }

    private int resolveEurekaServerCount(ConfigurableEnvironment environment) {
        return environment.getProperty(EUREKA_COUNT_PROPERTY, Integer.class, DEFAULT_EUREKA_SERVER_COUNT);
    }

    private void registerEurekaProperties(
            ConfigurableEnvironment environment,
            List<GenericContainer<?>> eurekaContainers
    ) {
        String defaultZone = eurekaContainers.stream()
                .map(this::toEurekaServiceUrl)
                .collect(Collectors.joining(","));

        addFirstPropertySource(
                environment,
                PROPERTY_SOURCE_NAME,
                Map.of(EUREKA_SERVICE_URL_PROPERTY, defaultZone)
        );
    }

    private String toEurekaServiceUrl(GenericContainer<?> eurekaContainer) {
        return "http://%s:%s/eureka/".formatted(
                eurekaContainer.getHost(),
                eurekaContainer.getMappedPort(EUREKA_INTERNAL_PORT)
        );
    }
}
