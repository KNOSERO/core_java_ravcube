# Stream

Stream udostępnia klientowi odczytowy kanał SSE. Jest lekkim informatorem o
zmianie zasobu, a nie kanałem przesyłającym obiekty biznesowe.

Klient subskrybuje jeden albo wiele identyfikatorów. Po zmianie otrzymuje
identyfikator zasobu i jego wersję, a następnie pobiera aktualny obiekt przez
zwykłe API REST.

## Instalacja

Aplikacja korzysta z publicznego modułu:

`kotlin
dependencies {
    implementation(project(":lib:stream:api"))
}
`

Aplikacja nie musi zależeć bezpośrednio od `stream:common` ani
`stream:core`.

## Konfiguracja Kafka

Transport odświeżenia Stream działa przez Kafka. Należy aktywować profil
`kafka` oraz ustawić stabilną nazwę serwisu:

`yaml
spring:
  profiles:
    active: kafka

ravcube:
  stream:
    kafka:
      service-name: claims-service
      # Opcjonalne. W Kubernetes powinno być unikalne dla każdego poda.
      instance-id: pod-1
`

`service-name` musi być takie samo dla wszystkich podów jednego serwisu i
różne dla innych serwisów. `instance-id` musi być unikalne dla każdego poda.
Jeżeli nie zostanie podane, biblioteka użyje wartości środowiskowej `HOSTNAME`,
a poza środowiskiem z `HOSTNAME` wygeneruje identyfikator losowy.

## Pierwsze użycie

Aplikacja dostarcza:

1. publikację `ClientStreamRefreshEvent` po udanej zmianie biznesowej;
2. zwykły endpoint REST, z którego klient pobierze aktualny obiekt;
3. wersję zasobu pochodzącą ze źródła prawdy.

### Subskrypcja i odczyt

`javascript
const stream = new EventSource("/streams/claims?ids=123&ids=456");

stream.addEventListener("refresh", async event => {
    const notification = JSON.parse(event.data);
    const response = await fetch("/claims/" + notification.resourceId);

    if (response.status === 404) {
        removeClaim(notification.resourceId);
        return;
    }

    renderClaim(await response.json());
});
`

Stream nie zna ścieżki REST aplikacji. Odpowiada wyłącznie za subskrypcję i
powiadomienie o zmianie. Identyfikator nie jest traktowany przez ten moduł jako
dana wrażliwa. Ochrona danych biznesowych pozostaje w zwykłym API REST.

## Jak działa subskrypcja

Subskrypcja określa:

- nazwę zasobu;
- zbiór identyfikatorów;
- połączenie SSE klienta.

Jeden zasób:

`http
GET /streams/claims/123
Accept: text/event-stream
`

Kilka zasobów:

`http
GET /streams/claims?ids=123&ids=456
Accept: text/event-stream
`

Oba warianty mają ten sam format eventu. Stream nie wysyła initial snapshotu.
Klient powinien pobrać stan początkowy przez REST.

Puste ID, brak parametru `ids` albo przekroczenie limitu subskrypcji kończy
się błędem HTTP. Domyślne limity to 100 ID w jednej subskrypcji i 1000
aktywnych subskrypcji.

## Jak działa refresh

Po udanej zmianie biznesowej aplikacja publikuje event przez istniejący moduł
`lib:event`:

`java
eventPublisher.publish(
        new ClientStreamRefreshEvent("claims", claimId, claimVersion)
);
`

Event jest obsługiwany dopiero po zakończeniu transakcji. Zawiera:

- `resourceName` — nazwę zasobu używaną do routingu;
- `resourceId` — identyfikator zmienionego zasobu;
- `version` — wersję pochodzącą ze źródła prawdy.

Nie zawiera payloadu zasobu. Wersja nie jest generowana przez Stream.

## Izolacja między serwisami i podami

Dla konfiguracji:

`yaml
ravcube:
  stream:
    kafka:
      service-name: claims-service
      instance-id: pod-1
`

biblioteka używa:

- topicu `stream.resource.refresh.claims-service.commit`;
- grupy konsumenckiej `stream-refresh.claims-service.pod-1`.

Wszystkie pody serwisu używają tego samego topicu, ale każdy pod ma własną
grupę konsumencką. Dzięki temu każdy pod otrzymuje własną kopię eventu i może
odświeżyć lokalne połączenia SSE.

Inny serwis, np. z `service-name: payments-service`, używa innego topicu.
Nie zmieniaj globalnego `spring.kafka.consumer.group-id` w celu konfiguracji
Stream — zmiana ma dotyczyć wyłącznie właściwości `ravcube.stream.kafka`.

SSE ma jeden format payloadu:

`text
event: refresh
data: {"resourceId":"123","version":42}
`

Aktualny obiekt jest pobierany dopiero przez REST klienta.

## Ogólny przepływ

`mermaid
sequenceDiagram
  participant Client as Klient
  participant SSE as Stream SSE
  participant API as REST API
  participant App as Aplikacja
  participant Kafka as Kafka topic per service
  participant PodA as Stream listener pod A
  participant PodB as Stream listener pod B

  Client->>SSE: subskrypcja resourceName + ID
  Client->>API: GET stanu początkowego
  App->>Kafka: RefreshEvent po commitcie
  Kafka-->>PodA: event w grupie pod-A
  Kafka-->>PodB: event w grupie pod-B
  PodA->>SSE: refresh(resourceId, version)
  PodB->>SSE: refresh(resourceId, version)
  SSE-->>Client: notification resourceId + version
  Client->>API: GET aktualnego zasobu
  API-->>Client: aktualny obiekt
`

Każdy pod otrzymuje event, ale Kafka nie jest magazynem stanu Stream. Po
reconnect klient powinien pobrać aktualny stan przez REST.

## Konfiguracja SSE

Domyślna ścieżka to `/streams`. Można ją zmienić przez
`ravcube.stream.path`.

`yaml
ravcube:
  stream:
    path: /streams
    timeout: PT30M
    max-ids-per-subscription: 100
    max-subscriptions: 1000
`

## Testowanie

Testy core sprawdzają routing SSE bez HTTP i Kafka. Testy API sprawdzają
prawdziwy przepływ HTTP + Kafka z użyciem modułu `test:kafka` i Testcontainers.

SSE jest kanałem bieżących powiadomień, a nie trwałym magazynem eventów.
