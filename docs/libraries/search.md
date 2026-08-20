# Search

Modules:

```text
lib:search:api
lib:search:core
```

`lib:search:core` wraps common Elasticsearch repository and query patterns.
It gives services a compact API for exact, text, range, nested, and composed
predicates.

## Main Contracts

| Type | Responsibility |
| --- | --- |
| `SearchService<T, ID>` | Base service for Elasticsearch documents. |
| `GenericSearchRepository<T, ID>` | Repository contract with custom query execution. |
| `SearchQuery` | Factory for Elasticsearch predicates. |
| `SearchPredicate` | Boolean composition wrapper around an Elasticsearch query. |

## Configuration

Enable Elasticsearch configuration with the `elasticsearch` Spring profile.

```yaml
spring:
  profiles:
    active: elasticsearch
```

The profile maps these properties into Spring Elasticsearch configuration:

| Property | Default |
| --- | --- |
| `ravcube.search.uris` | `http://localhost:9200` |
| `ravcube.search.username` | empty |
| `ravcube.search.password` | empty |
| `ravcube.search.connection-timeout` | `1s` |
| `ravcube.search.socket-timeout` | `30s` |

## Example

```java
@Service
class PolicyDocumentService extends SearchService<PolicyDocument, String> {

    List<PolicyDocument> findActiveForCustomer(String customerId) {
        return findAll(q -> q.term("customerId", customerId)
                .must(q.term("status", "ACTIVE")));
    }
}
```

Nested query:

```java
service.findAll(q -> q.in("claims").term("status", "OPEN"));
```

## Query operations

`SearchQuery` supports:

| Operation | Elasticsearch intent |
| --- | --- |
| `matchAll` | Match every document. |
| `term` / `terms` | Exact field values. |
| `match` / `matchPhrase` | Text search. |
| `prefix` / `wildcard` | Case-insensitive prefix and wildcard matches. |
| `exists` / `notExists` | Field presence checks. |
| `gt`, `gte`, `lt`, `lte`, `between` | Numeric ranges. |
| `fuzzy` | Fuzzy text search. |
| `queryString` | Raw query string expression. |
| `in(path)` | Nested query scope. |

`SearchPredicate` composes predicates with `must`, `should`, `mustNot`, and
`not`.

## Service operations

`SearchService` exposes find by id, find one, find all, paged search, count,
exists, save, save all, and delete operations through `GenericSearchRepository`.

## Testing

Use `test:elasticsearch` for indexing, mappings, nested queries, and real
Elasticsearch semantics. Unit-test pure predicate composition only when the
Elasticsearch server is not part of the behavior.
