package com.ravcube.test.postgresql;

import com.ravcube.test.common.env.BaseEnvironmentPostProcessor;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;

public final class PostgresqlTestEnvironmentPostProcessor
        extends BaseEnvironmentPostProcessor implements PostgresqlTestcontainerConstants {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!isProfileActive(environment, application, POSTGRESQL_PROFILE)) {
            return;
        }

        String version = environment.getProperty(POSTGRESQL_VERSION_PROPERTY, DEFAULT_POSTGRESQL_VERSION);
        String database = environment.getProperty(POSTGRESQL_DATABASE_PROPERTY, DEFAULT_POSTGRESQL_DATABASE);
        String username = environment.getProperty(POSTGRESQL_USERNAME_PROPERTY, DEFAULT_POSTGRESQL_USERNAME);
        String password = environment.getProperty(POSTGRESQL_PASSWORD_PROPERTY, DEFAULT_POSTGRESQL_PASSWORD);

        addFirstPropertySource(
                environment,
                PROPERTY_SOURCE_NAME,
                Map.of(
                        "spring.datasource.url", "jdbc:tc:postgresql:%s:///%s".formatted(version, database),
                        "spring.datasource.username", username,
                        "spring.datasource.password", password,
                        "spring.datasource.driver-class-name", "org.testcontainers.jdbc.ContainerDatabaseDriver",
                        "spring.jpa.hibernate.ddl-auto", "create-drop"
                )
        );
    }
}
