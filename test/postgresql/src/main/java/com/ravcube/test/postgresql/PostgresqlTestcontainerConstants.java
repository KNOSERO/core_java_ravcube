package com.ravcube.test.postgresql;

interface PostgresqlTestcontainerConstants {

    String PROPERTY_SOURCE_NAME = "ravcubeTestPostgresqlContainer";
    String POSTGRESQL_PROFILE = "test-postgresql";
    String POSTGRESQL_VERSION_PROPERTY = "ravcube.testcontainers.postgres.version";
    String POSTGRESQL_DATABASE_PROPERTY = "ravcube.testcontainers.postgres.database";
    String POSTGRESQL_USERNAME_PROPERTY = "ravcube.testcontainers.postgres.username";
    String POSTGRESQL_PASSWORD_PROPERTY = "ravcube.testcontainers.postgres.password";
    String DEFAULT_POSTGRESQL_VERSION = "18-alpine";
    String DEFAULT_POSTGRESQL_DATABASE = "test";
    String DEFAULT_POSTGRESQL_USERNAME = "test";
    String DEFAULT_POSTGRESQL_PASSWORD = "test";
}
