# Keycloak Test Module

Module:

```text
test:keycloak
```

Use this module when a test needs a real Keycloak server.

## Dependency

```kotlin
dependencies {
    testImplementation(project(":test:keycloak"))
}
```

## Profile

```java
import static com.ravcube.test.keycloak.KeycloakTestProfiles.TEST_KEYCLOAK_PROFILE;

@ActiveProfiles({"keycloak", TEST_KEYCLOAK_PROFILE})
```

## What this module configures

Injected properties:

| Property | Value |
| --- | --- |
| `ravcube.keycloak.issuer-uri` | `http://<host>:<mapped-port>/realms/<realm>` |

Override properties:

| Property | Default |
| --- | --- |
| `ravcube.testcontainers.keycloak.enabled` | `true` |
| `ravcube.testcontainers.keycloak.image` | `quay.io/keycloak/keycloak:26.0.7` |
| `ravcube.testcontainers.keycloak.realm` | `master` |

The container starts Keycloak in development mode with admin credentials
`admin` / `admin`. Use the production `keycloak` profile together with
`TEST_KEYCLOAK_PROFILE`.

## Good use cases

- token acquisition,
- token validation,
- auth endpoint integration,
- security filter behavior.

For pure authorization rules, prefer unit tests over a Keycloak container.
