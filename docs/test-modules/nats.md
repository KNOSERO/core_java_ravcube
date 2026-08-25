# NATS Test Module

Module:

```text
test:nats
```

Use `test:nats` when a test must prove broadcast behavior against a real NATS
server, especially when more than one application pod must receive the same
event.

## Dependency

```kotlin
dependencies {
    testImplementation(project(":test:nats"))
}
```

## Activate profiles

```java
import static com.ravcube.test.nats.NatsTestProfiles.TEST_NATS_PROFILE;

@SpringBootTest
@ActiveProfiles({"nats", TEST_NATS_PROFILE})
class StreamNatsIntegrationTest {
}
```

The `nats` profile enables the production event transport. The test profile
starts a reusable NATS Testcontainer and injects its URL into
`ravcube.nats.url`.

## What this module configures

| Property | Default |
| --- | --- |
| `ravcube.testcontainers.nats.enabled` | `true` |
| `ravcube.testcontainers.nats.image` | `nats:2.12-alpine` |
| `ravcube.nats.subject-prefix` | `event-core-test` |

Use this module for real subject routing, JSON serialization, after-commit
behavior, and broadcast tests with multiple subscribers. Use a unit test for
subject string validation that does not need a running NATS server.
