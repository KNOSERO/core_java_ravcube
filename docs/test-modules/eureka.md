# Eureka Test Module

Module:

```text
test:eureka
```

Use this module when a test needs real Eureka registration or discovery
behavior.

## Dependency

```kotlin
dependencies {
    testImplementation(project(":test:eureka"))
}
```

## Profile

```java
import static com.ravcube.test.eureka.EurekaTestProfiles.TEST_EUREKA_PROFILE;

@ActiveProfiles({"eureka", TEST_EUREKA_PROFILE})
```

## What this module configures

Injected properties:

| Property | Value |
| --- | --- |
| `eureka.client.service-url.defaultZone` | Comma-separated URLs for the started Eureka servers. |

Override properties:

| Property | Default |
| --- | --- |
| `ravcube.testcontainers.eureka.enabled` | `true` |
| `ravcube.testcontainers.eureka.image` | `steeltoeoss/eureka-server:4.1.1`, pinned by manifest digest. |
| `ravcube.testcontainers.eureka.count` | `1` |

The module only starts Eureka when the test profile is active and
`eureka.client.service-url.defaultZone` is missing or still points to the default
`http://localhost:8761/eureka/`.

The test profile also registers a primary `ServiceInstanceListSupplier` for the
current test service. This keeps local Feign/load-balancer calls deterministic
while the application still uses the Eureka-related profiles.

## Good use cases

- service registration,
- service discovery by name,
- OpenFeign client discovery,
- fault-tolerance behavior through discovered services.
