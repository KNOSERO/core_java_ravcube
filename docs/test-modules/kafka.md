# Kafka Test Module

Module:

```text
test:kafka
```

Use `test:kafka` when a test must prove behavior against a real Kafka broker.

## What problem this module solves

Kafka integration tests usually need the same boilerplate:

- start Kafka,
- wait until the broker is ready,
- expose bootstrap servers to Spring,
- use a stable test profile,
- cleanly reuse the container across tests.

This module centralizes that work. A test should describe event behavior, not
how Kafka was started.

## Dependency

```kotlin
dependencies {
    testImplementation(project(":test:kafka"))
}
```

## Activate profiles

```java
import static com.ravcube.test.kafka.KafkaTestProfiles.TEST_KAFKA_PROFILE;

@SpringBootTest
@ActiveProfiles({"kafka", TEST_KAFKA_PROFILE})
class PolicyEventKafkaIntegrationTest {
}
```

The `kafka` profile enables production Kafka configuration. The
`TEST_KAFKA_PROFILE` profile enables the reusable test container.

## What this module configures

Injected properties:

| Property | Value |
| --- | --- |
| `spring.kafka.bootstrap-servers` | Kafka Testcontainer bootstrap servers. |

Override properties:

| Property | Default |
| --- | --- |
| `ravcube.testcontainers.kafka.enabled` | `true` |
| `ravcube.testcontainers.kafka.image` | `confluentinc/cp-kafka:7.7.0` |

The test profile also sets Kafka listener startup to `true`, uses
`event-core-kafka-test` as the group id, starts from `earliest`, and trusts
`com.ravcube.lib.*` JSON packages.

## Good use cases

Use this module to verify:

- a publisher writes an event to Kafka,
- a listener consumes an event from Kafka,
- topic names are resolved correctly,
- Kafka headers are created correctly,
- serialization works with the configured infrastructure.

## Bad use cases

Do not use Kafka for tests that only check domain object creation.

Prefer a unit test for this:

```java
PolicyCreated event = new PolicyCreated("policy-1");

assertEquals("policy-1", event.getKey());
```

Use Kafka only when the broker is part of the behavior being tested.
