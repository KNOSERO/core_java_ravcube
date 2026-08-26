# Event

Biblioteka Event przekazuje typowane zdarzenia domenowe po zakończeniu transakcji.
Służy do uruchamiania reakcji lokalnie albo przez Kafka, bez wiązania kodu
domenowego z mechanizmem transportowym. Nie jest magazynem zdarzeń ani
mechanizmem odtwarzania historii.

## Moduły i granice

| Moduł | Odpowiedzialność | Zależności techniczne |
| --- | --- | --- |
| lib:event:common | DomainEvent, @Topic i EventPublisher, czyli neutralne kontrakty zdarzeń. | Brak Springa i Kafka. |
| lib:event:core | Implementacja EventPublisher, routing, cykl transakcji Spring i adapter Kafka. | Spring, Kafka i event:common. |
| lib:event:api | Fasada konfigurująca i udostępniająca Event na zewnątrz. | event:core i event:common. |

~~~mermaid
flowchart BT
    api["event:api"] --> core["event:core"]
    api --> common["event:common"]
    core --> common
~~~

Moduł domenowy, który jedynie definiuje zdarzenie, używa event:common:

~~~kotlin
dependencies {
    api(project(":lib:event:common"))
}
~~~

Kod aplikacyjny, który publikuje zdarzenia, używa wyłącznie event:api:

~~~kotlin
dependencies {
    implementation(project(":lib:event:api"))
}
~~~

event:api udostępnia kontrakty z event:common i dołącza event:core jako
wewnętrzną implementację. EventPublisher fizycznie należy do event:common,
dzięki czemu core może go implementować bez zależności zwrotnej do api. Moduł
konfiguracyjny, który rejestruje własne klasy Default...Publisher lub
Default...Listener, deklaruje dodatkowo event:core. Kod biznesowy nie powinien
korzystać z klas routingu ani adapterów Kafka.

## Pierwsze użycie

1. Zdefiniuj niezmienny typ zdarzenia i przypisz mu topic.
2. Zarejestruj publisher oraz listener dla potrzebnego sposobu dostarczenia.
3. Opublikuj zdarzenie wewnątrz transakcji przez EventPublisher.

~~~java
import com.ravcube.lib.event.DomainEvent;
import com.ravcube.lib.event.annotation.Topic;

@Topic("policy.created")
public record PolicyCreated(String policyId) implements DomainEvent {

    @Override
    public String getKey() {
        return policyId;
    }
}
~~~

Poniższa para obsługuje zdarzenie lokalnie po zatwierdzeniu transakcji:

~~~java
import com.ravcube.lib.event.listener.DefaultCommitListener;
import com.ravcube.lib.event.publisher.DefaultCommitPublisher;
import org.springframework.stereotype.Component;

@Component
class PolicyCreatedPublisher extends DefaultCommitPublisher<PolicyCreated> {
}

@Component
class PolicyCreatedListener extends DefaultCommitListener<PolicyCreated> {

    @Override
    public void on(PolicyCreated event) {
        // lokalna reakcja na utworzenie polisy
    }
}
~~~

~~~java
import com.ravcube.lib.event.api.EventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class PolicyService {

    private final EventPublisher eventPublisher;

    PolicyService(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    void create(String policyId) {
        // zapis polisy
        eventPublisher.publish(new PolicyCreated(policyId));
    }
}
~~~

Jeżeli dla typu zdarzenia nie ma zarejestrowanego publishera, publikacja jest
pomijana. Publisher należy więc rejestrować razem z obsługą zdarzenia.

## Przepływ lokalny

~~~mermaid
sequenceDiagram
    participant Service as Serwis domenowy
    participant Publisher as EventPublisher
    participant Spring as Cykl transakcji
    participant Handler as Listener

    Service->>Publisher: publish(PolicyCreated)
    Publisher->>Spring: zdarzenie aplikacyjne
    Spring-->>Handler: AFTER_COMMIT
    Handler->>Handler: reakcja
~~~

DefaultCommitPublisher przekazuje zdarzenie do Spring, a
DefaultCommitListener otrzymuje je w fazie AFTER_COMMIT. Rollback nie uruchamia
takiego listenera. Dla zachowania związanego wyłącznie z rollbackiem użyj
DefaultRollbackPublisher i DefaultRollbackListener.

## Kafka

Kafka włączasz profilem kafka:

~~~yaml
spring:
  profiles:
    active: kafka
~~~

Dla komunikacji przez Kafka rejestruj publisher oraz listener w tym profilu:

~~~java
import com.ravcube.lib.event.listener.DefaultKafkaCommitListener;
import com.ravcube.lib.event.publisher.DefaultKafkaPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("kafka")
class PolicyCreatedKafkaPublisher extends DefaultKafkaPublisher<PolicyCreated> {
}

@Component
@Profile("kafka")
class PolicyCreatedKafkaListener extends DefaultKafkaCommitListener<PolicyCreated> {

    @Override
    public void on(PolicyCreated event) {
        // reakcja po odbiorze rekordu Kafka
    }
}
~~~

Dla KAFKA_AFTER_COMMIT zdarzenie jest wysyłane po zatwierdzeniu transakcji na
topic <wartość @Topic>.commit. DomainEvent.getKey() jest kluczem rekordu Kafka;
domyślnie zwraca pusty łańcuch. Nadpisz go, gdy partycjonowanie lub kolejność
dla konkretnej encji mają znaczenie.

~~~mermaid
sequenceDiagram
    participant Service as Serwis domenowy
    participant Spring as Cykl transakcji
    participant Kafka as Kafka
    participant Handler as Listener Kafka

    Service->>Spring: publish(PolicyCreated)
    Spring->>Kafka: AFTER_COMMIT, policy.created.commit
    Kafka-->>Handler: PolicyCreated
~~~

| Sposób dostarczenia | Publisher | Listener | Moment wywołania / topic |
| --- | --- | --- | --- |
| Lokalnie po commit | DefaultCommitPublisher | DefaultCommitListener | AFTER_COMMIT w tej samej JVM. |
| Lokalnie po rollbacku | DefaultRollbackPublisher | DefaultRollbackListener | AFTER_ROLLBACK w tej samej JVM. |
| Kafka po commit | DefaultKafkaPublisher | DefaultKafkaCommitListener | <topic>.commit po zatwierdzeniu transakcji. |
| Kafka po rollbacku | DefaultKafkaRollbackPublisher | DefaultKafkaRollbackListener | <topic>.rollback po wycofaniu transakcji. |

Domyślna grupa konsumentów Eventu, event-core-kafka, obsługuje zwykłe zdarzenia
konkurencyjnie; nie jest mechanizmem broadcastu do każdego poda. Stream ma
własny topic i grupę konsumentów, ponieważ refresh SSE musi trafić do każdego
poda utrzymującego lokalne połączenia klientów.

## Konfiguracja Kafka i jej właściciel

event:core jest właścicielem wspólnych domyślnych ustawień Kafka w
application-kafka.yml. Aplikacja ustawia połączenie z własnym brokerem, na
przykład spring.kafka.bootstrap-servers. Stream nie nadpisuje tej konfiguracji;
posiada wyłącznie usługowe ustawienia topicu, grupy i polityki odbioru refreshy.

| Właściwość | Domyślna wartość |
| --- | --- |
| ravcube.kafka.listener.auto-startup | true dla profilu kafka |
| ravcube.kafka.listener.missing-topics-fatal | false |
| ravcube.kafka.consumer.group-id | event-core-kafka |
| ravcube.kafka.consumer.auto-offset-reset | latest |
| ravcube.kafka.consumer.trusted-packages | * |

Serializacja producenta i deserializacja konsumenta są domyślnie oparte o
Springowy JSON. W produkcji zawęź ravcube.kafka.consumer.trusted-packages do
pakietów zdarzeń aplikacji; wartość * jest zgodna wstecznie, lecz zbyt szeroka.

## Zgodność migracji

DomainEvent, @Topic i EventPublisher zachowały nazwy pakietów. Moduł domenowy
zmienia wyłącznie zależność Gradle z event:api na event:common. Kod publikujący
dodaje event:api, który udostępnia kontrakt z event:common i składa go z
implementacją event:core. Import punktu wejścia pozostaje bez zmian:
com.ravcube.lib.event.api.EventPublisher. Poprzedni import z błędnie nazwanej
paczki inteface jest zachowany jako przestarzały alias do stopniowej migracji.

Niskopoziomowy SPI routingu nie jest już częścią event:api. Kod, który
bezpośrednio implementował AbstractListener, AbstractPublisher lub korzystał z
EventSource, powinien zależeć od event:core i przejść na klasy z pakietu
com.ravcube.lib.event.routing. W typowych przypadkach wystarczą gotowe klasy
Default...Publisher i Default...Listener pokazane wyżej.
