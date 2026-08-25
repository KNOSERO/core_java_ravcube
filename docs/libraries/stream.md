# Stream

## Wyjaśnienie

Stream udostępnia kanał SSE do obserwowania zmian zasobów. Klient najpierw pobiera
stan przez REST, potem otwiera subskrypcję, a po zmianie otrzymuje tylko:

~~~json
{"resourceId":"123","version":42}
~~~

Stream nie wysyła payloadu biznesowego. Po otrzymaniu refresh klient odczytuje
aktualny stan z własnego endpointu REST. resourceId nie jest traktowany jako
dana wrażliwa; właściwa autoryzacja dostępu do stanu pozostaje po stronie
endpointu REST właściciela zasobu.

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

Nie dodawaj bezpośredniej zależności na stream:core. Common zawiera publiczny
kontrakt eventu i błąd pojemności, a implementacja SSE i Kafka pozostaje
wewnętrzna.

## Konfiguracja

Profil stream dostarcza:

~~~yaml
ravcube:
  stream:
    path: /streams
    timeout: PT30M
    heartbeat-interval: PT15S
    max-ids-per-subscription: 100
    max-subscriptions: 1000
    max-subscriptions-per-client: 100
    max-pending-events-per-subscription: 100
~~~

Wartości można nadpisać:

~~~yaml
ravcube:
  stream:
    path: /custom-streams
    timeout: PT10M
    heartbeat-interval: PT20S
    max-ids-per-subscription: 50
    max-subscriptions: 500
    max-subscriptions-per-client: 20
    max-pending-events-per-subscription: 50
~~~

Znaczenie limitów:

- max-ids-per-subscription ogranicza liczbę ID w jednym połączeniu.
- max-subscriptions ogranicza liczbę aktywnych połączeń w jednej JVM.
- max-subscriptions-per-client ogranicza połączenia z jednego adresu klienta.
- max-pending-events-per-subscription chroni pamięć przed wolnym klientem.

Po przekroczeniu kolejki wolna subskrypcja jest zamykana. Wysyłanie odbywa się
na izolowanych virtual threads, więc wolny klient nie blokuje wspólnej małej puli
platformowych wątków. Limity HTTP i dodatkowy rate limiting powinny być nadal
egzekwowane na gatewayu lub ingressie.

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
wysyła initial snapshotu.

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

Połączenie wysyła okresowy komentarz SSE heartbeat oraz ustawia klientowi
opóźnienie reconnectu na 3 sekundy. Heartbeat nie jest eventem biznesowym i nie
zmienia wersji zasobu. Po reconnect klient powinien ponownie pobrać stan przez
REST.

Błędy subskrypcji:

| Sytuacja | HTTP |
| --- | --- |
| brak ID, puste ID lub niepoprawne parametry | 400 Bad Request |
| przekroczenie liczby ID albo limitu aktywnych połączeń | 429 Too Many Requests |

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
Dla tego typu powiadomień biblioteka stosuje maksymalnie trzy asynchroniczne próby
publikacji do Kafka. Retry jest ograniczony tylko do ClientStreamRefreshEvent;
inne eventy korzystające z DefaultKafkaPublisher zachowują swoją dotychczasową
politykę.

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

Listener Stream korzysta z własnego KafkaListenerContainerFactory i własnego
retry/error handling. Wspólne pozostają wyłącznie ustawienia połączenia z
brokerem i serializerów dostarczane przez aplikację.

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

version musi być monotoniczną wersją zasobu w ramach resourceName i resourceId.
Jeśli aplikacja używa wersji z bazy danych, klient może bezpiecznie odrzucać
starsze powiadomienia.

Po reconnect klient powinien ponownie otworzyć SSE, pobrać aktualny stan przez
REST i porównać version z lokalnym stanem. SSE jest kanałem powiadomień, a nie
magazynem historii.

## Obserwowalność

Jeśli aplikacja udostępnia Micrometer, Stream rejestruje:

- ravcube.stream.subscriptions.active
- ravcube.stream.subscriptions.rejected
- ravcube.stream.events.queue.overflow
- ravcube.stream.events.send.failure
- ravcube.stream.heartbeat.failure

Logger zapisuje odrzucenia, rozłączenia wolnych klientów, błędy wysyłki SSE oraz
błędy heartbeatów. Nie loguj payloadu biznesowego — Stream go nie posiada.

## Testowanie

Testy stream:core sprawdzają zachowanie SSE bez HTTP, Spring events i Kafka:
routing po ID, wersje, limity globalne i per klient, cleanup oraz obsługę wolnego
klienta.

Testy stream:api używają pełnego Spring context, realnego klienta HTTP,
realnego boundary transakcji i Testcontainers Kafka. Sprawdzają przepływ:

~~~text
HTTP subscription
    -> EventPublisher
    -> AFTER_COMMIT
    -> Kafka Testcontainer
    -> stream-specific listener factory
    -> lokalny rejestr SSE
    -> HTTP SSE client
~~~

Test limitu połączeń działa w osobnym kontekście Spring, żeby nie wpływać na
pozostałe testy. Nie zastępuj tego przepływu mockami.
