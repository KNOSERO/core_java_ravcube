# Stream

The Stream modules provide a small Spring MVC implementation for server-sent
events (SSE). They let an application subscribe to one or more resource ids and
receive single-resource refreshes.

## Modules

```text
lib:stream:api
lib:stream:core
```

`lib:stream:api` contains the framework-independent resource handler and
publisher contracts. `lib:stream:core` contains the in-memory SSE registry,
Spring MVC endpoints, and the default publisher.

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

## HTTP endpoints

The default base path is `/streams`. Override it with
`ravcube.stream.path`. The emitter timeout defaults to `PT30M` and can be
changed with `ravcube.stream.timeout`.

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

The registry is process-local and in-memory. It is suitable for one application
instance. In a multi-instance deployment, use the existing `lib:event` module
to distribute an after-commit refresh request to every instance before calling
the local publisher.

SSE is a live notification channel, not a durable event store. After a
reconnect, the client should load the current resource through the normal
authorized read API. Authentication and resource-level authorization remain the
application's responsibility.
