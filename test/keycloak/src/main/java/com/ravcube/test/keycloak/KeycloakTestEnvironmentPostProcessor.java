package com.ravcube.test.keycloak;

import com.ravcube.test.common.container.SharedContainer;
import com.ravcube.test.common.env.BaseTestcontainerEnvironmentPostProcessor;
import java.util.Map;
import org.springframework.core.env.ConfigurableEnvironment;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public final class KeycloakTestEnvironmentPostProcessor
        extends BaseTestcontainerEnvironmentPostProcessor<GenericContainer<?>>
        implements KeycloakTestcontainerConstants {

    private static final SharedContainer<GenericContainer<?>> SHARED_KEYCLOAK_CONTAINER = new SharedContainer<>();

    @Override
    protected SharedContainer<GenericContainer<?>> sharedContainer() {
        return SHARED_KEYCLOAK_CONTAINER;
    }

    @Override
    protected String propertySourceName() {
        return PROPERTY_SOURCE_NAME;
    }

    @Override
    protected String profile() {
        return KEYCLOAK_PROFILE;
    }

    @Override
    protected String enabledProperty() {
        return KEYCLOAK_ENABLED_PROPERTY;
    }

    @Override
    protected String imageProperty() {
        return KEYCLOAK_IMAGE_PROPERTY;
    }

    @Override
    protected String defaultImage() {
        return DEFAULT_KEYCLOAK_IMAGE;
    }

    @Override
    protected String shutdownHookName() {
        return KEYCLOAK_SHUTDOWN_HOOK_NAME;
    }

    @Override
    protected java.util.function.Predicate<GenericContainer<?>> runningChecker() {
        return GenericContainer::isRunning;
    }

    @Override
    protected GenericContainer<?> createContainer(String imageName) {
        return new GenericContainer<>(DockerImageName.parse(imageName))
                .withExposedPorts(KEYCLOAK_INTERNAL_PORT)
                .withEnv("KEYCLOAK_ADMIN", KEYCLOAK_ADMIN_USER)
                .withEnv("KEYCLOAK_ADMIN_PASSWORD", KEYCLOAK_ADMIN_PASSWORD)
                .withCommand("start-dev");
    }

    @Override
    protected Map<String, Object> properties(GenericContainer<?> container, ConfigurableEnvironment environment) {
        String issuerUri = "http://%s:%s/realms/%s".formatted(
                container.getHost(),
                container.getMappedPort(KEYCLOAK_INTERNAL_PORT),
                environment.getProperty(KEYCLOAK_REALM_PROPERTY, DEFAULT_KEYCLOAK_REALM)
        );

        return Map.of(SPRING_ISSUER_URI_PROPERTY, issuerUri);
    }
}
