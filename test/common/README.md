# test:common

Instrukcja operacyjna tworzenia nowego modulu `test:*` opartego o Testcontainers.

Ten dokument ma sluzyc do wykonywania pracy, nie do ogolnego opisu.

## Cel

Gdy pojawia sie potrzeba dodania nowego modulu testcontainer, trzeba utworzyc
modul zgodny z istniejacym wzorcem repo.

Przykladowy wynik:

- `test:elasticsearch`
- `test:redis`
- `test:kafka`
- `test:keycloak`

## Typowe zadania

Przyklady:

- "dodaj testcontainer dla Elasticsearch"
- "zrob reusable test module dla Redis"
- "stworz modul test:* jak Kafka"

## Wynik oczekiwany

Po zakonczeniu pracy nowy modul ma:

- dawac sie dodac przez `testImplementation(project(":test:<name>"))`
- uruchamiac kontener tylko przy aktywnym profilu `test-<name>`
- trzymac wspolna konfiguracje w swoim `application.yml` i `application-test-<name>.yml`
- wystawiac publiczna klase `*TestProfiles`
- automatycznie ustawiac potrzebne Spring properties
- miec lokalny `README.md` z instrukcja uzycia i linkiem do tego pliku

## Decyzja techniczna

Najpierw trzeba zdecydowac, czy nowy modul jest:

1. prostym modulem z jednym kontenerem
2. modulem z niestandardowa logika startu
3. modulem opartym o `jdbc:tc` lub inny mechanizm niewymagajacy `EnvironmentPostProcessor`

### Jesli to prosty modul

Nalezy uzyc:

- `BaseTestcontainerEnvironmentPostProcessor`
- `SharedContainer`

Przyklady:

- Redis
- Kafka
- Elasticsearch
- Keycloak

### Jesli to modul niestandardowy

Nalezy uzyc:

- `BaseEnvironmentPostProcessor`
- `SharedContainerCluster` tylko gdy jest potrzebny klaster albo wiele instancji

Przyklad:

- Eureka

### Jesli to modul oparty o `jdbc:tc`

Nalezy:

- trzymac konfiguracje w `application.yml` i `application-test-<name>.yml`
- wystawic `*TestProfiles`
- nie dodawac `EnvironmentPostProcessor`, jesli nie jest potrzebny

Przyklad:

- PostgreSQL

## Procedura tworzenia nowego modulu

Przyklad referencyjny: `test:elasticsearch`

### Krok 1. Dodaj stale

Trzeba utworzyc `*TestcontainerConstants`.

Plik ma zawierac co najmniej:

- nazwe profilu, np. `test-elasticsearch`
- nazwe property source
- `enabled` property
- `image` property
- domyslny image
- nazwy Spring properties uzupelnianych po starcie kontenera
- opcjonalnie port wewnetrzny i nazwe shutdown hook

### Krok 2. Dodaj klase `*TestProfiles`

Trzeba utworzyc publiczna klase:

```java
public final class ElasticsearchTestProfiles {

    public static final String TEST_ELASTICSEARCH_PROFILE =
            ElasticsearchTestcontainerConstants.ELASTICSEARCH_PROFILE;

    private ElasticsearchTestProfiles() {
    }
}
```

Ta klasa jest wymagana.

Testy w innych modulach nie powinny wpisywac stringa
`"test-elasticsearch"` recznie.

### Krok 3. Dodaj `EnvironmentPostProcessor`

#### Wariant prosty

Nalezy dziedziczyc po `BaseTestcontainerEnvironmentPostProcessor`.

Trzeba zaimplementowac:

- `sharedContainer()`
- `propertySourceName()`
- `profile()`
- `enabledProperty()`
- `imageProperty()`
- `defaultImage()`
- `shutdownHookName()`
- `runningChecker()`
- `createContainer(...)`
- `properties(...)`

`properties(...)` ma mapowac dane kontenera na Spring properties
potrzebne przez modul docelowy.

Przyklad dla Elasticsearch:

- `spring.elasticsearch.uris`

Przyklad dla Redis:

- `spring.data.redis.host`
- `spring.data.redis.port`

#### Wariant niestandardowy

Jesli potrzebna jest specjalna logika, trzeba napisac wlasny
`EnvironmentPostProcessor` na bazie `BaseEnvironmentPostProcessor`.

To dotyczy przypadkow takich jak:

- budowa URL z wielu instancji
- klaster
- dodatkowe warunki startu

#### Wariant bez `EnvironmentPostProcessor`

Jesli modul dziala w calosci przez konfiguracyjny mechanizm Spring albo `jdbc:tc`,
nie trzeba dodawac `EnvironmentPostProcessor`.

W takim przypadku wspolna logika ma pozostac w YAML i `*TestProfiles`.

### Krok 4. Zarejestruj post processor

Trzeba dodac wpis do:

- `src/main/resources/META-INF/spring.factories`

Przyklad:

```properties
org.springframework.boot.env.EnvironmentPostProcessor=\
com.ravcube.test.elasticsearch.ElasticsearchTestEnvironmentPostProcessor
```

Jesli nowy post processor nie zostanie tu zarejestrowany,
modul jest niekompletny.

Ten krok dotyczy tylko modulow, ktore rzeczywiscie maja `EnvironmentPostProcessor`.

### Krok 5. Dodaj konfiguracje YAML

Trzeba utworzyc dwa pliki:

- `application.yml`
- `application-test-<name>.yml`

#### Zasada dla `application.yml`

Ten plik ma zawierac:

- domyslne wartosci
- `enabled: false`

Ten plik nie ma sam aktywowac kontenera.

#### Zasada dla `application-test-<name>.yml`

Ten plik ma zawierac:

- `enabled: true`
- dodatkowe wspolne ustawienia testowe dla tego profilu, jesli sa potrzebne

Przyklad dla Eureka:

- szybsze interwaly klienta
- testowe `eureka.instance.*`

### Krok 6. Dodaj README modulu

Trzeba utworzyc `README.md` w nowym module `test:*`.

README modulu ma:

- linkowac do `test/common/README.md`
- pokazywac jak dodac zaleznosc
- pokazywac jak aktywowac profil
- pokazywac jakie properties sa ustawiane automatycznie
- pokazywac czego nie wpisywac recznie w testach

## Instrukcja uzycia gotowego modulu w module docelowym

Jesli gotowy modul `test:*` ma zostac wdrozony w bibliotece lub aplikacji,
trzeba wykonac ponizsze kroki.

### Krok 1. Dodaj zaleznosc testowa

Przyklad:

```kotlin
testImplementation(project(":test:elasticsearch"))
```

### Krok 2. Aktywuj profil bazowy i testowy

Przyklad:

```java
import static com.ravcube.test.elasticsearch.ElasticsearchTestProfiles.TEST_ELASTICSEARCH_PROFILE;

@ActiveProfiles({"elasticsearch", TEST_ELASTICSEARCH_PROFILE})
```

### Krok 3. Nie kopiuj wspolnej konfiguracji do testu

Nie nalezy:

- przepisywac `spring.elasticsearch.uris`
- przepisywac `spring.data.redis.host`
- przepisywac wspolnych ustawien Eureka
- duplikowac `enabled` i `image` w testach modulu docelowego

To ma zostac w module `test:*`.

### Krok 4. Zostaw tylko lokalne override

W testach docelowych moga zostac tylko ustawienia specyficzne dla testu,
np.:

- `server.port`
- testowy `group-id`
- pojedynczy override image lub credentiali, jesli test tego wymaga

## Kryteria zakonczenia

Prace sa zakonczone dopiero gdy:

- nowy modul kompiluje sie
- ma `application.yml`
- ma `application-test-<name>.yml`
- ma `*TestProfiles`
- ma lokalny `README.md`
- README modulu linkuje do tego pliku

Jesli modul korzysta z `EnvironmentPostProcessor`, dodatkowo:

- ma zarejestrowany `EnvironmentPostProcessor`

## Zakazy

Nie nalezy:

- omijac rzeczywistego kontenera lokalnym stubem, jesli celem jest prawdziwy test integracyjny
- trzymac wspolnej konfiguracji w tescie modulu docelowego
- hardcodowac nazwy profili w wielu miejscach, jesli mozna wystawic `*TestProfiles`
- zostawiac `enabled: true` w glownym `application.yml`
