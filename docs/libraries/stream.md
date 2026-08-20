# Stream

Modules:

```text
lib:stream:api
lib:stream:core
```

The stream library exposes server-sent events for client-facing refreshes.

`lib:stream:api` owns stream abstractions and refresh events. `lib:stream:core`
owns the Spring MVC SSE endpoints and refresh handling.

## Stream shapes

| Shape | Example |
| --- | --- |
| Resource | One claim by id. |
| Collection | All claims. |
| Selected collection | Claims `1,2,3`. |

## Basic resource stream

```java
@Component
class PolicyClaimStream extends ClientResourceStream<String, PolicyClaimDto> {

    PolicyClaimStream(ClientStreamPublisher publisher) {
        super(publisher);
    }

    @Override
    protected String resourceName() {
        return "policies.claims";
    }
}
```

Publish refresh:

```java
policyClaimStream.refresh(claimId, payload);
```

## REST-refreshable stream

Use `ClientRestResourceStream` when the stream can rebuild payload from an id.

```java
@Component
class PolicyClaimRestStream extends ClientRestResourceStream<String, PolicyClaimDto> {

    PolicyClaimRestStream(ClientStreamPublisher publisher) {
        super(publisher);
    }

    @Override
    public String resourceName() {
        return "policies.claims";
    }

    @Override
    protected PolicyClaimDto payload(String id) {
        return claimQuery.load(id);
    }
}
```

Refresh endpoint:

```http
POST /streams/updates/policies.claims/{claimId}
```

## HTTP endpoints

The base path is configurable with:

| Property | Default |
| --- | --- |
| `ravcube.stream.path` | `/streams` |
| `ravcube.stream.timeout` | `PT30M` |

Subscription endpoints:

```http
GET /streams/{resourceName}
GET /streams/{resourceName}?ids=1&ids=2
GET /streams/{resourceName}/{resourceId}
```

Manual refresh endpoints:

```http
POST /streams/updates/{resourceName}
POST /streams/updates/{resourceName}?ids=1&ids=2
POST /streams/updates/{resourceName}/{resourceId}
```

Initial snapshots are sent as `refresh` events when a matching REST-refreshable
handler exists.

## Naming

`ClientStreamNames` creates stable stream names:

| Shape | Name |
| --- | --- |
| Collection | `<resourceName>` |
| Resource | `<resourceName>.<resourceId>` |
| Selected collection | `<resourceName>.<id1>,<id2>` |

## Event-backed refresh

`ClientEventResourceStream`, `ClientEventCollectionStream`, and
`ClientEventSelectedCollectionStream` publish typed refresh-requested events
instead of refreshing locally. When a `KafkaTemplate` bean exists, stream core
registers Kafka publishers for those events. The listeners consume
`KAFKA_AFTER_COMMIT` events and call the local update handlers.

## Design warning

Tests should talk about client behavior: "listening client receives refresh".
Hide HTTP and SSE plumbing in test support helpers.
