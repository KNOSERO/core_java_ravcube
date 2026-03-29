package com.ravcube.test.keycloak;

import com.ravcube.test.common.container.SharedContainer;
import com.ravcube.test.common.env.BaseEnvironmentPostProcessor;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public final class KeycloakTestEnvironmentPostProcessor
        extends BaseEnvironmentPostProcessor implements KeycloakTestcontainerConstants {

    private static final SharedContainer<GenericContainer<?>> SHARED_KEYCLOAK_CONTAINER = new SharedContainer<>();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!isProfileActive(environment, application, KEYCLOAK_PROFILE)
                || !isEnabled(environment, KEYCLOAK_ENABLED_PROPERTY)) {
            return;
        }

        String keycloakRealm = resolveKeycloakRealm(environment);
        GenericContainer<?> keycloakContainer = SHARED_KEYCLOAK_CONTAINER.start(
                resolveKeycloakImage(environment),
                this::createKeycloakContainer,
                GenericContainer::isRunning,
                KEYCLOAK_SHUTDOWN_HOOK_NAME
        );
        registerKeycloakProperties(environment, keycloakContainer, keycloakRealm);
    }

    private String resolveKeycloakImage(ConfigurableEnvironment environment) {
        return environment.getProperty(KEYCLOAK_IMAGE_PROPERTY, DEFAULT_KEYCLOAK_IMAGE);
    }

    private GenericContainer<?> createKeycloakContainer(String imageName) {
        return new GenericContainer<>(DockerImageName.parse(imageName))
                .withExposedPorts(KEYCLOAK_INTERNAL_PORT)
                .withEnv("KEYCLOAK_ADMIN", KEYCLOAK_ADMIN_USER)
                .withEnv("KEYCLOAK_ADMIN_PASSWORD", KEYCLOAK_ADMIN_PASSWORD)
                .withCommand("start-dev");
    }

    private String resolveKeycloakRealm(ConfigurableEnvironment environment) {
        return environment.getProperty(KEYCLOAK_REALM_PROPERTY, DEFAULT_KEYCLOAK_REALM);
    }

    private void registerKeycloakProperties(ConfigurableEnvironment environment,
                                            GenericContainer<?> keycloakContainer,
                                            String keycloakRealm) {
        String issuerUri = "http://%s:%s/realms/%s".formatted(
                keycloakContainer.getHost(),
                keycloakContainer.getMappedPort(KEYCLOAK_INTERNAL_PORT),
                keycloakRealm
        );

        addFirstPropertySource(environment, PROPERTY_SOURCE_NAME, Map.of(SPRING_ISSUER_URI_PROPERTY, issuerUri));
    }
}
