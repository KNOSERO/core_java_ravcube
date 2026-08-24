# Stream

The Stream modules provide a small Spring MVC implementation for server-sent
events (SSE). They let an application expose live refreshes of one resource,
a whole collection, or a selected set of resource ids.

## Modules

\`\`\`text
lib:stream:api
lib:stream:core
\`\`\`

\`lib:stream:api\` contains the framework-independent contracts used by the
application. \`lib:stream:core\` contains the in-memory SSE registry, Spring MVC
endpoints, and the default publisher.

## Dependency

Add the implementation module to a Spring application:

\`\`\`kotlin
dependencies {
    implementation(project(":lib:stream:core"))
}
\`\`\`

The core module exposes the API module transitively.

## Public API

All resource ids are represented as strings at the HTTP boundary. A service can
implement the matching interface and use the injected \`ClientStreamPublisher\`:

\`\`\`java
@Component
final class PolicyClaimStream implements ClientRestResourceStream<PolicyClaimDto> {

    private final ClientStreamPublisher publisher;

    PolicyClaimStream(ClientStreamPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public ClientStreamPublisher publisher() {
        return publisher;
    }

    @Override
    public String resourceName() {
        return "policies.claims";
    }

    @Override
    public PolicyClaimDto resource(String resourceId) {
        return claimQuery.load(resourceId);
    }
}
\`\`\`

\`ClientRestResourceStream<T>\` combines the resource subscription contract with
the update handler. Calling \`update(id)\` loads the current resource through
\`resource(id)\` and publishes it to the stream.

For a manual publication, use:

\`\`\`java
policyClaimStream.refresh(claimId, payload);
\`\`\`

For collection-shaped payloads use \`ClientCollectionStream<T>\`, and for a
selected set of ids use \`ClientSelectedCollectionStream<T>\`:

\`\`\`java
collectionStream.refresh(claims);
selectedStream.refresh(List.of("claim-1", "claim-2"), claims);
\`\`\`

## HTTP endpoints

The default base path is \`/streams\`. Override it with
\`ravcube.stream.path\`. The emitter timeout defaults to \`PT30M\` and can be
changed with \`ravcube.stream.timeout\`.

| Method | Endpoint | Behavior |
| --- | --- | --- |
| \`GET\` | \`/streams/{resourceName}/{resourceId}\` | subscribes to one resource; sends an initial \`refresh\` event when a matching \`ClientRestResourceStream\` exists |
| \`GET\` | \`/streams/{resourceName}\` | subscribes to updates for the whole collection |
| \`GET\` | \`/streams/{resourceName}?ids=1&ids=2\` | subscribes to updates matching exactly the selected ids |
| \`POST\` | \`/streams/updates/{resourceName}/{resourceId}\` | loads the resource through the matching handler and publishes its current value |

The update endpoint returns \`204 No Content\`. It returns \`404 Not Found\` when
the handler or resource does not exist.

A resource update is sent as a \`refresh\` SSE event to:

- the subscriber of that exact resource;
- all subscribers of the resource collection;
- selected-collection subscribers that contain the updated id.

The payload is serialized by Spring MVC using the application's normal message
converters. Apply the application's regular authentication and authorization
rules to these endpoints.

## SSE event format

Every refresh is sent with the SSE event name \`refresh\`:

\`\`\`text
event: refresh
data: <serialized payload>
\`\`\`

The registry is process-local and in-memory. It is intended for a single
application instance. For multiple instances, publish the same refresh to every
instance through an external event mechanism before calling the local publisher.

## Naming

\`ClientStreamNames\` provides stable names for resource shapes:

| Shape | Name |
| --- | --- |
| Collection | \`<resourceName>\` |
| Resource | \`<resourceName>.<resourceId>\` |
| Selected collection | \`<resourceName>.<sorted-id-1>,<sorted-id-2>\` |

The helper removes duplicate selected ids and sorts them, so equivalent id
selections produce the same name.

## Related modules

- \`lib:event:api\` and \`lib:event:core\` can be used to distribute refresh requests
  between application instances.
- \`lib:common\` contains shared infrastructure used by other library modules.
