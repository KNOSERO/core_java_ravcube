# Idempotency

Module:

```text
lib:idempotency:core
```

Idempotency protects command endpoints from being executed twice when the client
retries the same request.

## Responsibility

This module integrates `idempotency4j` with Spring MVC. If a `CacheStore` bean
exists, idempotency entries are stored through cache. With Redis enabled, this
becomes distributed idempotency.

If no `CacheStore` bean exists, the module falls back to the in-memory
`idempotency4j` store. Use the cache-backed store for behavior that must survive
multiple application instances.

## Dependency

```kotlin
dependencies {
    implementation(project(":lib:idempotency:core"))
    implementation(project(":lib:cache:api"))
    implementation(project(":lib:cache:core"))
}
```

## Example endpoint

```java
@RestController
class PaymentController {

    private final PaymentService paymentService;

    PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Idempotent(ttl = "PT1H")
    @PostMapping("/payments")
    PaymentResponse create(@RequestBody PaymentRequest request) {
        return paymentService.create(request);
    }
}
```

The client sends:

```http
POST /payments
Idempotency-Key: 7abf7f83-8f70-4f81-93dd-3fbaaa269302
Content-Type: application/json
```

## Explain

The first request executes the controller. A repeated request with the same key
and the same body returns the stored response. A repeated key with a different
body is rejected by the idempotency library because the fingerprint changed.

## Configuration

The cache-backed store uses this property:

| Property | Default | Purpose |
| --- | --- | --- |
| `ravcube.idempotency.key-prefix` | `idempotency` | Prefix for entries stored in `CacheStore`. |

With Redis-backed cache, activate both the Redis profile and the idempotency
starter configuration:

```java
@Idempotent(ttl = "PT1H", lockTimeout = "PT10S")
@PostMapping("/payments")
PaymentResponse create(@RequestBody PaymentRequest request) {
    return paymentService.create(request);
}
```

## Runtime behavior

- New requests create an in-progress entry with a lock timeout.
- Completed responses are stored for the annotation TTL.
- A completed duplicate with the same fingerprint returns the stored response.
- A duplicate with a different fingerprint is rejected by the idempotency
  library.
- Failed entries are retained briefly so a retry can acquire the key again.
- The cache-backed store uses compare-and-replace operations, so distributed
  behavior depends on the selected `CacheStore` implementation.

## Design warning

Idempotency is for command endpoints. Do not use it to hide non-deterministic
business logic. The command itself should still be designed as a clear use case.
