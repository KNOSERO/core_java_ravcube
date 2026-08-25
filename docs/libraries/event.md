# Event

Modules:

```text
lib:event:api
lib:event:core
```

The event library provides typed domain events and Spring, NATS, and Kafka
infrastructure for publishing and listening.

## Responsibility

`lib:event:api` owns the domain event contract. `lib:event:core` owns transport
and framework integration.

Events are routed by event type and source. Every `DomainEvent` that participates
in routing must have `@Topic`.

## Event example

```java
@Topic("policy.created")
public record PolicyCreated(String policyId) implements DomainEvent {

    @Override
    public String getKey() {
        return policyId;
    }
}
```

## Publisher example

```java
@Component
class PolicyCreatedPublisher extends DefaultCommitPublisher<PolicyCreated> {
}
```

## Listener example

```java
@Component
class PolicyCreatedListener extends DefaultCommitListener<PolicyCreated> {

    @Override
    public void on(PolicyCreated event) {
        // handle event after transaction commit
    }
}
```

## Explain

Use commit publishers/listeners when the event should be visible only after a
successful transaction. Use rollback variants only for behavior that explicitly
belongs to a failed transaction path.

## Event sources

| Source | Publisher/listener type | Topic behavior |
| --- | --- | --- |
| `SPRING_AFTER_COMMIT` | `DefaultCommitPublisher`, `DefaultCommitListener` | In-process after transaction commit. |
| `SPRING_AFTER_ROLLBACK` | `DefaultRollbackPublisher`, `DefaultRollbackListener` | In-process after transaction rollback. |
| `KAFKA_AFTER_COMMIT` | `DefaultKafkaPublisher`, `DefaultKafkaCommitListener` | Kafka topic `<topic>.commit`. |
| `KAFKA_AFTER_ROLLBACK` | `DefaultKafkaRollbackPublisher`, `DefaultKafkaRollbackListener` | Kafka topic `<topic>.rollback`. |
| `NATS_BROADCAST` | `DefaultNatsPublisher`, `DefaultNatsCommitListener` | Broadcast to every active pod on `<subject-prefix>.<topic>`. |

Kafka records use `DomainEvent.getKey()` as the record key. The default key is
an empty string, so domain events that need partitioning or ordering should
override `getKey()`.

## Kafka configuration

Enable Kafka transport with the `kafka` Spring profile.

```yaml
spring:
  profiles:
    active: kafka
```

Default properties are namespaced under `ravcube.kafka.*` and mapped into Spring
Kafka listener, producer, and consumer settings. Important defaults:

| Property | Default |
| --- | --- |
| `ravcube.kafka.listener.auto-startup` | `true` |
| `ravcube.kafka.listener.missing-topics-fatal` | `false` |
| `ravcube.kafka.consumer.group-id` | `event-core-kafka` |
| `ravcube.kafka.consumer.auto-offset-reset` | `latest` |
| `ravcube.kafka.consumer.trusted-packages` | `*` |

## NATS configuration

Enable NATS transport with the `nats` Spring profile:

```yaml
spring:
  profiles:
    active: nats

ravcube:
  nats:
    url: nats://nats:4222
    subject-prefix: claims-service
```

`subject-prefix` is a service namespace. All pods of one service must share it;
different services should use different values. The NATS adapter subscribes
without a queue group, so every pod receives the event. It uses the existing
after-commit publisher boundary and serializes only the typed event contract.

Core NATS is a live broadcast channel: it does not persist messages for later
replay. Use it for refresh signals where the consumer can read the current state
again. Use JetStream only when replay or durable delivery is part of the
business contract.

## Design warning

Do not publish anonymous maps or strings when the event is part of the domain.
Create a typed `DomainEvent` with an explicit topic.
