# PostgreSQL Test Module

Module:

```text
test:postgresql
```

Use this module when a test needs a real PostgreSQL database.

## Dependency

```kotlin
dependencies {
    testImplementation(project(":test:postgresql"))
}
```

## Profile

```java
import static com.ravcube.test.postgresql.PostgresqlTestProfiles.TEST_POSTGRESQL_PROFILE;

@ActiveProfiles({"postgresql", TEST_POSTGRESQL_PROFILE})
```

## What this module configures

The module uses Testcontainers JDBC instead of manually starting a container.
When `test-postgresql` is active, it registers:

| Property | Value |
| --- | --- |
| `spring.datasource.url` | `jdbc:tc:postgresql:<version>:///<database>` |
| `spring.datasource.username` | configured test username |
| `spring.datasource.password` | configured test password |
| `spring.datasource.driver-class-name` | `org.testcontainers.jdbc.ContainerDatabaseDriver` |
| `spring.jpa.hibernate.ddl-auto` | `create-drop` |

Override properties:

| Property | Default |
| --- | --- |
| `ravcube.testcontainers.postgres.version` | `18-alpine` |
| `ravcube.testcontainers.postgres.database` | `test` |
| `ravcube.testcontainers.postgres.username` | `test` |
| `ravcube.testcontainers.postgres.password` | `test` |

## Good use cases

- JPA mappings,
- migrations,
- repository queries,
- transaction behavior,
- database-specific SQL.

Use a unit test instead when behavior does not depend on the database.
