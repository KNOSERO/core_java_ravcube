# Event

Modules:

```text
lib:event:api
lib:event:core
```

The event library provides typed domain events and Spring/Kafka infrastructure
for publishing and listening.

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

## Scoped Kafka topic

The default Kafka publisher uses the topic from `Topic`. A specialized
publisher may override `baseTopic(event)` when one event family must be
isolated per service:

```java
@Component
@Profile("kafka")
final class ServiceRefreshPublisher extends DefaultKafkaPublisher<ServiceRefresh> {

    @Override
    protected String baseTopic(ServiceRefresh event) {
        return "service.refresh." + serviceName;
    }
}
```

The publisher still uses the Kafka commit suffix. The matching listener must
subscribe to the same resolved topic. This extension is opt-in and does not
change the topic resolution of existing publishers.

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

## Design warning

Do not publish anonymous maps or strings when the event is part of the domain.
Create a typed `DomainEvent` with an explicit topic.
