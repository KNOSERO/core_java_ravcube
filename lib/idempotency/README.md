# lib:idempotency

Modul integruje `idempotency4j` ze Spring MVC i istniejacym `CacheStore`.

## Moduly

- `lib:idempotency:core` - auto-konfiguracja Spring Boot oraz adapter `CacheStoreIdempotencyStore`
- `lib:cache:api` - abstrakcja cache uzywana jako storage
- `lib:cache:core` - implementacja Redis dla `CacheStore`

## Jak uzyc

Dodaj zaleznosci:

```kotlin
implementation(project(":lib:idempotency:core"))
implementation(project(":lib:cache:api"))
implementation(project(":lib:cache:core"))
```

Zabezpiecz endpoint adnotacja z `idempotency4j`:

```java
import io.github.josipmusa.idempotency.spring.web.Idempotent;

@RestController
class PaymentController {

    @Idempotent(ttl = "PT1H")
    @PostMapping("/payments")
    PaymentResponse createPayment(@RequestBody PaymentRequest request) {
        return paymentService.create(request);
    }
}
```

Klient wysyla naglowek:

```http
POST /payments
Idempotency-Key: 1b0db8cc-1124-4f87-a9df-56a40c33ac2a
Content-Type: application/json
```

Pierwszy strzal wykonuje kontroler. Drugi strzal z tym samym `Idempotency-Key` i tym samym body
nie wykonuje kontrolera ponownie, tylko zwraca zapisana pierwsza odpowiedz z naglowkiem:

```http
Idempotent-Replayed: true
```

Ten sam klucz z innym body jest odrzucany przez `idempotency4j` jako konflikt fingerprintu.

## Storage

Jesli w kontekscie Spring istnieje `CacheStore`, modul tworzy `CacheStoreIdempotencyStore`.
W profilu `redis` oznacza to Redis z `lib:cache:core`.

```yaml
ravcube:
  idempotency:
    key-prefix: idempotency
```

Jesli `CacheStore` nie istnieje, modul tworzy fallback `InMemoryIdempotencyStore` z biblioteki
`idempotency4j`. To jest dobre do testow lokalnych, ale nie do kilku instancji aplikacji.
