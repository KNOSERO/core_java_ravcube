package com.ravcube.test.keycloak;

interface KeycloakTestcontainerConstants {

    String PROPERTY_SOURCE_NAME = "ravcubeTestKeycloakContainer";
    String KEYCLOAK_PROFILE = "test-keycloak";
    String KEYCLOAK_ENABLED_PROPERTY = "ravcube.testcontainers.keycloak.enabled";
    String KEYCLOAK_IMAGE_PROPERTY = "ravcube.testcontainers.keycloak.image";
    String KEYCLOAK_REALM_PROPERTY = "ravcube.testcontainers.keycloak.realm";
    String SPRING_ISSUER_URI_PROPERTY = "spring.security.oauth2.resourceserver.jwt.issuer-uri";
    int KEYCLOAK_INTERNAL_PORT = 8080;
    String DEFAULT_KEYCLOAK_IMAGE = "quay.io/keycloak/keycloak:26.0.7";
    String DEFAULT_KEYCLOAK_REALM = "master";
    String KEYCLOAK_ADMIN_USER = "admin";
    String KEYCLOAK_ADMIN_PASSWORD = "admin";
    String KEYCLOAK_SHUTDOWN_HOOK_NAME = "ravcube-test-keycloak-stop";
}
