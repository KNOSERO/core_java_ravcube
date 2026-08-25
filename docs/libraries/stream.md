# Stream

The Stream modules provide a small Spring MVC implementation for server-sent
events (SSE). They let an application subscribe to one or more resource ids and
receive single-resource refreshes.

## Modules

```text
lib:stream:api
lib:stream:core
```

`lib:stream:api` contains the framework-independent resource handler, publisher,
and authorization contracts. `lib:stream:core` contains the in-memory SSE
registry, Spring MVC endpoints, and the default publisher.

The source is intentionally split by responsibility:

```text
lib/stream/api/src/main/java/com/ravcube/lib/stream/api
  ClientRestResourceStream.java       # public resource/update contract
  ClientStreamPublisher.java          # public refresh contract
  ClientStreamAuthorizer.java         # public access contract

lib/stream/core/src/main/java/com/ravcube/lib/stream
  domain/                              # subscription rules, no Spring/SSE
  application/                         # resource catalog and update use case
  infrastructure/config/              # configurable runtime properties
  infrastructure/sse/                 # SseEmitter registry and publisher
  web/                                 # HTTP/SSE transport adapter
```

Tests follow the same package structure. A class belongs in the smallest
package that owns its responsibility; do not place unrelated domain, transport,
configuration, and infrastructure classes in the root package.

The public API package is `com.ravcube.lib.stream.api`. Existing consumers that
imported contracts from `com.ravcube.lib.stream` must update those imports after
this package reorganization.

## Dependency

Add the implementation module to a Spring application:

```kotlin
dependencies {
    implementation(project(":lib:stream:core"))
}
```

The core module exposes the API module transitively.

## Public API

A resource handler provides the resource name and loads the current resource by
id. Its `update(id)` method loads the resource and publishes one update:

```java
import com.ravcube.lib.stream.api.ClientRestResourceStream;
import com.ravcube.lib.stream.api.ClientStreamPublisher;

@Component
final class PolicyClaimStream implements ClientRestResourceStream<PolicyClaimDto> {

    private final ClientStreamPublisher publisher;

    PolicyClaimStream(ClientStreamPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public String resourceName() {
        return "policies.claims";
    }

    @Override
    public ClientStreamPublisher publisher() {
        return publisher;
    }

    @Override
    public PolicyClaimDto resource(String resourceId) {
        return claimQuery.load(resourceId);
    }
}

policyClaimStream.update(claimId);
```

If the resource does not exist, `update(id)` returns `false` and nothing is
published.

For a payload that is already loaded, the publisher exposes one operation:

```java
publisher.publish("policies.claims", claimId, payload);
```

The application must provide a resource-level authorizer. The stream module does
not assume that authentication alone grants access:

```java
@Bean
ClientStreamAuthorizer streamAuthorizer(CurrentUser currentUser) {
    return (resourceName, resourceIds) -> resourceId ->
            claimAccess.canRead(currentUser, resourceName, resourceId);
}
```

The authorization decision is checked when the subscription is created and
again before each event is sent. The refresh endpoint uses the same decision.
This allows a revoked permission to stop future events for an existing
connection and prevents refresh requests for unauthorized resources.

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
| `GET` | `/streams/{resourceName}/{resourceId}` | subscribes to one resource and sends an initial `refresh` event when a matching handler returns a payload |
| `GET` | `/streams/{resourceName}?ids=1&ids=2` | subscribes to the selected resource ids |
| `POST` | `/streams/updates/{resourceName}/{resourceId}` | loads and publishes one resource through the matching handler |

The update endpoint returns `204 No Content`. It returns `404 Not Found`
when no matching handler exists or the handler returns `null`.

Every update is sent as one `refresh` SSE event to subscriptions that contain
the updated id. A subscription for ids `1,2` therefore receives the update for
`1`, but not the update for `3`.

## SSE event format

Every update uses the event name `refresh`:

```text
event: refresh
data: <serialized resource payload>
```

When a publisher is called inside an active Spring transaction, the event is
sent after a successful commit. A rollback therefore does not notify clients.

The registry is process-local and in-memory. It is suitable for one application
instance. In a multi-instance deployment, an application must distribute a
small `{resourceName, resourceId}` refresh message through the existing
`lib:event` module after commit, then load the resource and call the local
publisher on every instance. The stream module deliberately does not choose a
Kafka/Redis deployment for the application.

SSE is a live notification channel, not a durable event store. After a
reconnect, the client should load the current resource through the normal
authorized read API. The selected-ids endpoint is a notification subscription
and does not send an initial collection snapshot.
