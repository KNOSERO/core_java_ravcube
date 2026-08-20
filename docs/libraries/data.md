# Data

Module:

```text
lib:data
```

`lib:data` provides reusable Spring Data JPA patterns for services that use
QueryDSL predicates and Blaze Persistence Entity Views.

## Main Contracts

| Type | Responsibility |
| --- | --- |
| `EntityClass<ID>` | Minimal entity identifier contract. |
| `GenericRepository<T, ID>` | JPA repository with QueryDSL and Entity View support. |
| `EntityViewExecutor<T>` | Executes Blaze Entity View projections from QueryDSL predicates. |
| `GenericService<T, Q, ID>` | Common read/write operations built around QueryDSL paths. |

## Use When

Use this module when a JPA-backed service follows common repository patterns:
find one, find all, page, count, exists, create, and delete.

Do not use it when a use case needs a more explicit domain API. Generic service
helpers should reduce boilerplate, not hide business behavior.

## Example

```java
interface PolicyRepository extends GenericRepository<Policy, Long> {
}
```

```java
@Service
class PolicyService extends GenericService<Policy, QPolicy, Long> {

    Optional<Policy> findByNumber(String number) {
        return findOne(policy -> policy.number.eq(number));
    }
}
```

Entity View projection:

```java
List<PolicySummaryView> summaries =
        policyService.findAll(policy -> policy.customerId.eq(customerId), PolicySummaryView.class);
```

## Configuration

`BlazeConfig` enables JPA repositories and Entity Views under:

```text
ravcube.data.base-packages
```

Default:

```text
com.ravcube
```

Set this property when an application keeps repositories or entity views outside
the default package tree.

## Service operations

`GenericService` provides:

| Operation | Purpose |
| --- | --- |
| `findOne` | Find one entity by QueryDSL predicate. |
| `findAll` | Find entities by predicate, sort, or QueryDSL order specifiers. |
| `findAll(..., Class<R>)` | Find Entity View projections. |
| `findPage` | Find a Spring `Page`. |
| `count` / `exists` | Query aggregate checks. |
| `create` | Save an entity. |
| `delete` | Delete by id or entity id. |
| `treat` | Build QueryDSL type treatment for inheritance queries. |

## Testing

Use `test:postgresql` when query behavior depends on real database mappings,
SQL generation, transactions, or Blaze Entity View integration.
