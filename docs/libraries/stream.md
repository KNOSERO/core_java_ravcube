# Stream

## Wyjaśnienie

Stream udostępnia kanał SSE do obserwowania zmian zasobów. Klient najpierw pobiera
stan przez REST, potem otwiera subskrypcję, a po zmianie otrzymuje tylko:

~~~json
{"resourceId":"123","version":42}
~~~

Stream nie wysyła payloadu biznesowego. Po otrzymaniu refresh klient odczytuje
aktualny stan z własnego endpointu REST.

~~~mermaid
sequenceDiagram
    participant Client as Klient
    participant REST as REST aplikacji
    participant PodA as Pod serwisu A
    participant Kafka as Kafka topic serwisu

    Client->>REST: GET /claims/123
    REST-->>Client: stan początkowy
    Client->>PodA: GET /streams/claims?ids=123
    PodA-->>Client: połączenie SSE
    PodA->>Kafka: ClientStreamRefreshEvent po commit
    Kafka-->>PodA: event grupy poda
    PodA-->>Client: refresh resourceId + version
    Client->>REST: GET /claims/123
    REST-->>Client: aktualny stan
~~~

## Instalacja

~~~kotlin
dependencies {
    implementation(project(":lib:stream:api"))
}
~~~

Nie dodawaj bezpośredniej zależności na stream:core. Common zawiera tylko
kontrakt eventu, a implementacja SSE i Kafka pozostaje wewnętrzna.

## Konfiguracja

Profil stream dostarcza:

~~~yaml
ravcube:
  stream:
    path: /streams
    timeout: PT30M
    max-ids-per-subscription: 100
    max-subscriptions: 1000
    max-pending-events-per-subscription: 100
~~~

Wartości można nadpisać:

~~~yaml
ravcube:
  stream:
    path: /custom-streams
    timeout: PT10M
    max-ids-per-subscription: 50
    max-subscriptions: 500
    max-pending-events-per-subscription: 50
~~~

Limit pending events chroni pamięć przed klientem, który odbiera dane wolniej niż
są publikowane. Po przekroczeniu limitu klient jest rozłączany. Limity HTTP i
rate limiting dla użytkowników powinny być dodatkowo egzekwowane na gatewayu
lub ingressie.

## Pierwsze użycie

Pojedynczy zasób:

~~~http
GET /streams/claims/123
Accept: text/event-stream
~~~

Wiele zasobów:

~~~http
GET /streams/claims?ids=123&ids=456
Accept: text/event-stream
~~~

Oba endpointy tworzą ten sam typ subskrypcji: nazwę zasobu i zbiór ID. Stream nie
wysyła initial snapshotu. Puste ID oraz przekroczenie limitów zwracają 400 Bad
Request.

Przykład klienta:

~~~javascript
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
~~~

## Publikacja refresh

~~~java
eventPublisher.publish(
        new ClientStreamRefreshEvent("claims", claimId, claimVersion)
);
~~~

Publikacja musi nastąpić wewnątrz aktywnej transakcji. Biblioteka publikuje
wiadomość do Kafka dopiero po AFTER_COMMIT. Publikacja poza transakcją kończy się
błędem, a rollback nie wysyła refreshu.

Event zawiera wyłącznie resourceName, resourceId i version ze źródła prawdy.

## Wiele podów i izolacja serwisów

Dla spring.application.name=claims-service oraz Kubernetesowego HOSTNAME:

~~~text
topic:          stream.resource.refresh.claims-service.commit
consumer group: stream-refresh.claims-service.<hostname>
~~~

~~~mermaid
graph LR
    Event[Refresh claims] --> Topic[stream.resource.refresh.claims-service.commit]
    Topic --> GroupA[stream-refresh.claims-service.pod-a]
    Topic --> GroupB[stream-refresh.claims-service.pod-b]
    GroupA --> RegistryA[Rejestr SSE poda A]
    GroupB --> RegistryB[Rejestr SSE poda B]
    RegistryA --> ClientA[Klient A]
    RegistryB --> ClientB[Klient B]
~~~

Każdy pod tego samego serwisu ma własną grupę konsumencką, więc każdy pod
otrzymuje event i aktualizuje własne połączenia SSE. Inny serwis ma inny topic.
Biblioteka nie zmienia globalnego spring.kafka.consumer.group-id ani routingu
innych eventów.

Można jawnie nadpisać:

~~~yaml
ravcube:
  stream:
    kafka:
      service-name: claims-service
      instance-id: pod-a
~~~

W Kubernetes nie ustawiaj stałego instance-id w ConfigMap dla wszystkich replik.
Domyślne użycie HOSTNAME zapewnia różne grupy.

## Semantyka wersji i reconnect

Kafka oraz SSE mogą dostarczyć duplikat lub zdarzenie nieprzydatne po reconnect.
Rejestr pomija dla danej subskrypcji powiadomienie z wersją mniejszą lub równą
ostatniej zaakceptowanej wersji.

Po reconnect klient powinien ponownie otworzyć SSE, pobrać aktualny stan przez
REST i porównać version z lokalnym stanem. SSE jest kanałem powiadomień, a nie
magazynem historii.

## Testowanie

Testy stream:core sprawdzają zachowanie SSE bez HTTP, Spring events i Kafka:
routing po ID, wersje, limity, kolejność, cleanup i obsługę wolnego klienta.

Testy stream:api używają pełnego Spring context, realnego klienta HTTP,
realnego boundary transakcji i Testcontainers Kafka. Sprawdzają przepływ:

~~~text
HTTP subscription
    -> EventPublisher
    -> AFTER_COMMIT
    -> Kafka Testcontainer
    -> stream listener
    -> lokalny rejestr SSE
    -> HTTP SSE client
~~~

Nie zastępuj tego przepływu mockami.
