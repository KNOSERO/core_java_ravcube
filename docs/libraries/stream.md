# Stream

## Wyjaśnienie

Stream jest biblioteką do informowania klienta, że wskazany zasób został zmieniony.
Nie przesyła obiektu biznesowego. Zdarzenie SSE zawiera tylko:

~~~json
{"resourceId":"123","version":42}
~~~

Po otrzymaniu powiadomienia klient odczytuje aktualny stan przez zwykły endpoint
REST. Dzięki temu autoryzacja pozostaje przy endpointcie właściciela zasobu, a
Stream nie przenosi danych biznesowych przez wspólny kanał.

Subskrypcja pojedynczego i wielu elementów używa tego samego modelu. Aktualizacja
jednego ID trafia wyłącznie do subskrypcji, które zawierają to ID.

## Instalacja

~~~kotlin
dependencies {
    implementation(project(":lib:stream:api"))
}
~~~

Aplikacja korzystająca z biblioteki musi mieć dostęp do Kafka dla profilu
kafka. Nie należy dodawać bezpośredniej zależności na stream:core; implementacja
SSE pozostaje szczegółem biblioteki.

## Pierwsze użycie

Klient otwiera subskrypcję:

~~~http
GET /streams/claims?ids=123&ids=456
Accept: text/event-stream
~~~

Następnie aplikacja publikuje po udanej zmianie zasobu:

~~~java
eventPublisher.publish(
        new ClientStreamRefreshEvent("claims", claimId, claimVersion)
);
~~~

Publikacja musi być wykonana wewnątrz aktywnej transakcji. Po AFTER_COMMIT
Stream rozsyła refresh do podów, a klient ponownie pobiera:

~~~http
GET /claims/123
~~~

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

## Główny przepływ

~~~mermaid
sequenceDiagram
    participant Client as Klient
    participant REST as REST właściciela zasobu
    participant PodA as Pod serwisu
    participant Kafka as Topic serwisu

    Client->>REST: GET /claims/123
    REST-->>Client: stan początkowy
    Client->>PodA: GET /streams/claims?ids=123
    PodA-->>Client: połączenie SSE
    PodA->>Kafka: refresh po AFTER_COMMIT
    Kafka-->>PodA: event grupy poda
    PodA-->>Client: resourceId + version
    Client->>REST: GET /claims/123
    REST-->>Client: aktualny stan
~~~

## Wiele podów i izolacja serwisów

Dla spring.application.name=claims-service i Kubernetesowego HOSTNAME:

~~~text
topic:          stream.resource.refresh.claims-service.commit
consumer group: stream-refresh.claims-service.<hostname>
~~~

~~~mermaid
graph LR
    Event[Refresh claims] --> Topic[Topic claims-service]
    Topic --> GroupA[Grupa pod-a]
    Topic --> GroupB[Grupa pod-b]
    GroupA --> RegistryA[SSE registry poda A]
    GroupB --> RegistryB[SSE registry poda B]
    RegistryA --> ClientA[Klient A]
    RegistryB --> ClientB[Klient B]
~~~

Każdy pod tego samego serwisu ma własną grupę konsumencką, więc każdy otrzymuje
refresh i aktualizuje własne połączenia SSE. Inny serwis ma inny topic. Stream
nie zmienia globalnej grupy Kafka ani routingu pozostałych eventów.

## Konfiguracja

Domyślna konfiguracja:

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

Wartości można nadpisać. Limity są lokalne dla jednej instancji aplikacji:

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
- max-subscriptions ogranicza liczbę połączeń w jednej JVM.
- max-subscriptions-per-client ogranicza połączenia dla rozpoznanego klienta.
- max-pending-events-per-subscription chroni pamięć przed wolnym klientem.

Dla żądań HTTP identyfikator klienta pochodzi najpierw z uwierzytelnionego
principal. Jeśli aplikacja nie udostępnia principal, używany jest adres połączenia.
Nie należy ufać dowolnym nagłówkom forwarded bez konfiguracji zaufanego ingressu.
Dodatkowy rate limiting powinien być realizowany na gatewayu lub ingressie.

## Zachowanie SSE

- Stream nie wysyła initial snapshotu.
- Heartbeat jest komentarzem SSE, nie eventem biznesowym.
- Klient otrzymuje retry ustawione na 3 sekundy.
- Po reconnect klient powinien ponownie pobrać stan przez REST.
- Starsza lub równa wersja zasobu jest pomijana.
- version musi być monotoniczna dla pary resourceName + resourceId.
- Przepełnienie kolejki zamyka zbyt wolną subskrypcję.

Błędy subskrypcji:

| Sytuacja | HTTP |
| --- | --- |
| brak ID, puste ID lub niepoprawne parametry | 400 Bad Request |
| przekroczenie limitu ID lub aktywnych połączeń | 429 Too Many Requests |

## Publikacja Kafka

Refresh jest publikowany wyłącznie przez obsługę Stream i dopiero po AFTER_COMMIT.
Listener Stream korzysta z osobnej fabryki Kafka. Dziedziczy z fabryki aplikacji
consumer factory oraz najważniejsze ustawienia kontenera, a nadpisuje tylko
concurrency i error handling Stream.

Wspólna konfiguracja połączenia, serializacji i domyślnej grupy Kafka należy do
`lib:event:core` (`ravcube.kafka.*`). Stream nie nadpisuje tych wartości;
konfiguruje wyłącznie własny topic oraz grupę konsumencką wyprowadzane z
`spring.application.name`, `ravcube.stream.kafka.service-name` i
`ravcube.stream.kafka.instance-id`.

Stream wykonuje maksymalnie trzy próby publikacji z jednosekundowym backoffem.
Po wyczerpaniu prób powiadomienie jest traktowane jako utracone i rejestrowane w
metryce. Jest to kanał best-effort notification, a nie trwała historia zdarzeń.
Klient musi wykonać ponowny odczyt REST po reconnect lub wykryciu braku aktualizacji.

Pozostałe eventy korzystające z DefaultKafkaPublisher zachowują dotychczasową
politykę, ponieważ backoff i callback są używane wyłącznie przez publisher Stream.

## Obserwowalność

Jeśli aplikacja udostępnia Micrometer, Stream rejestruje:

- ravcube.stream.subscriptions.active
- ravcube.stream.subscriptions.rejected
- ravcube.stream.events.queue.overflow
- ravcube.stream.events.send.failure
- ravcube.stream.events.publish.failure
- ravcube.stream.heartbeat.failure

Logi zawierają wyłącznie techniczny kontekst: zasób, temat, klucz techniczny
oraz rodzaj błędu. Stream nie loguje payloadu biznesowego.

## Testowanie

Testy stream:core działają bez HTTP, Kafka i Spring events. Sprawdzają obserwowalne
zachowanie SSE: routing po ID, wersje, limity, unsubscribe, cleanup, heartbeat
oraz usuwanie wolnego klienta.

Testy stream:api używają pełnego Spring context, realnego klienta HTTP,
realnej granicy transakcji i Kafka Testcontainer. Sprawdzają:

~~~text
HTTP subscription
    -> EventPublisher
    -> AFTER_COMMIT
    -> Kafka Testcontainer
    -> stream-specific listener factory
    -> lokalny registry SSE
    -> HTTP SSE client
~~~

Weryfikowane są także routing do właściwej subskrypcji oraz brak refreshu po
rollbacku. Testy nie zastępują tych granic mockami.
