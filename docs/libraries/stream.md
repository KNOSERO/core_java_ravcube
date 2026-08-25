# Stream

The Stream modules provide a read-only Spring MVC implementation for
server-sent events (SSE). Resource refreshes are triggered only by the existing
`lib:event` module.

## Modules

```text
lib:stream:common
lib:stream:api
lib:stream:core
```

The dependency direction is:

```text
application -> stream:api
stream:api -> stream:common (api)
stream:api -> stream:core (implementation)
stream:core -> stream:common (implementation)
stream:common -> lib:event:api
stream:api -> lib:event:core
```

`lib:stream:common` contains all shared contracts: external read interfaces and
the refresh event. `lib:stream:api` is the public facade used by applications:
it re-exports `common` and brings the `core` implementation at runtime.
The API module contains the Spring MVC controller and event adapters; they call
the application service from `lib:stream:core`. The core module contains the
service, SSE registry, read handlers, and domain rules.

```text
lib/stream/common/src/main/java/com/ravcube/lib/stream
  api/                                # shared read contracts
    ClientStreamResourceReader.java
    ClientStreamAuthorization.java
  common/event/
    ClientStreamRefreshEvent.java     # shared event contract

lib/stream/api/build.gradle.kts        # public facade: common + core runtime
lib/stream/api/src/main/java/com/ravcube/lib/stream
  web/                                # read-only HTTP/SSE controller
  event/                              # lib:event adapters using core service

lib/stream/core/src/main/java/com/ravcube/lib/stream
  application/                         # service, resource catalog and limits
  infrastructure/config/              # runtime properties
  infrastructure/sse/                  # SseEmitter registry
  domain/                              # subscription rules
```

An external application depends only on the facade:

```kotlin
dependencies {
    implementation(project(":lib:stream:api"))
}
```

## Read contracts

An application provides a reader for each resource type:

```java
import com.ravcube.lib.stream.api.ClientStreamResourceReader;

@Component
final class PolicyClaimStream implements ClientStreamResourceReader<PolicyClaimDto> {

    @Override
    public String resourceName() {
        return "policies.claims";
    }

    @Override
    public PolicyClaimDto resource(String resourceId) {
        return claimQuery.load(resourceId);
    }
}
```

The application also provides resource-level authorization:

```java
@Bean
ClientStreamAuthorization streamAuthorization(ClaimAccess claimAccess) {
    return (resourceName, resourceId) ->
            claimAccess.canRead(CurrentUserContext.current(), resourceName, resourceId);
}
```

These contracts are read-only. They do not publish refreshes and do not expose
SSE implementation details.

## Refresh event

A successful business operation publishes the shared event through the
existing event module:

```java
import com.ravcube.lib.event.inteface.EventPublisher;
import com.ravcube.lib.stream.common.event.ClientStreamRefreshEvent;

eventPublisher.publish(new ClientStreamRefreshEvent("policies.claims", claimId));
```

With the default profile, `stream:api` registers the event publisher and
listener using `DefaultCommitPublisher` and `DefaultCommitListener`. With the
`nats` profile, it uses `DefaultNatsPublisher` and
`DefaultNatsCommitListener`. In both cases the refresh is handled after commit,
loads the current resource, rechecks access for every SSE subscription, and
sends one `refresh` event to matching resource ids.

The event contains only `resourceName` and `resourceId`; the payload is loaded
after commit and is not transported through the event bus.

## Multiple service pods

Enable the `nats` profile when the same service can have multiple pods:

```yaml
spring:
  profiles:
    active: nats

ravcube:
  nats:
    url: nats://nats:4222
    subject-prefix: claims-service
```

All pods of one service must use the same `subject-prefix`. Different services
must use different prefixes. The stream adapter uses a normal NATS broadcast
subscription, without a queue group, so every pod receives the refresh signal.
Each pod then reads the resource from its own source of truth and sends the
payload only to its local authorized SSE clients. The payload is never sent
through NATS.

NATS Core is intentionally used here as a transient notification channel. A
message published while a pod is disconnected is not replayed; after an SSE
reconnect the client receives the current state through the normal read path.
If durable replay or guaranteed delivery becomes a business requirement, this
design should be replaced or extended with JetStream and an explicit replay
contract.

## HTTP endpoints

The default base path is `/streams`. Override it with
`ravcube.stream.path`. The emitter timeout defaults to `PT30M` and can be
changed with `ravcube.stream.timeout`. Resource limits default to 100 ids per
subscription and 1000 active subscriptions:

```yaml
ravcube:
  stream:
    timeout: PT30M
    max-ids-per-subscription: 100
    max-subscriptions: 1000
```

| Method | Endpoint | Behavior |
| --- | --- | --- |
| `GET` | `/streams/{resourceName}/{resourceId}` | subscribes to one resource and sends an initial `refresh` event when a reader returns a payload |
| `GET` | `/streams/{resourceName}?ids=1&ids=2` | subscribes to selected resource ids |

There is no update endpoint and no Feign update client. Refreshes enter the
module only as `ClientStreamRefreshEvent` events.

## SSE event format

Every refresh uses the event name `refresh`:

```text
event: refresh
data: <serialized resource payload>
```

SSE is a live notification channel, not a durable event store. After a
reconnect, the client should load the current resource through the normal
authorized read API. The selected-ids endpoint is a notification subscription
and does not send an initial collection snapshot.

Without the `nats` profile, the Spring transport remains process-local and is
appropriate only for a single application instance. The `stream` module does
not depend directly on the NATS client; NATS is an implementation of the
transport in `lib:event:core`.
