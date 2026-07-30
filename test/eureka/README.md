# test:eureka

Instrukcja tworzenia nowych modulow `test:*` jest opisana w [test/common/README.md](../common/README.md).

Ten modul sluzy do podpiecia testowego kontenera Eureka w innych modulach.

## Jak zainstalowac do testow

Dodaj zaleznosc:

```kotlin
testImplementation(project(":test:eureka"))
```

## Jak uzyc w tescie modulu docelowego

Aktywuj profil bazowy i testowy:

```java
import static com.ravcube.test.eureka.EurekaTestProfiles.TEST_EUREKA_PROFILE;

@ActiveProfiles({"eureka", TEST_EUREKA_PROFILE})
```

## Co modul ustawia automatycznie

- start kontenera Eureka
- `eureka.client.service-url.defaultZone`
- wspolne testowe ustawienia klienta Eureka z `application-test-eureka.yml`

## Czego nie wpisywac recznie w module docelowym

Nie duplikuj w testach:

- `ravcube.eureka.client.registry-fetch-interval-seconds`
- `ravcube.eureka.client.instance-info-replication-interval-seconds`
- `ravcube.eureka.instance.lease-renewal-interval-in-seconds`
- `eureka.instance.hostname`
- `eureka.instance.ip-address`

## Co moze zostac lokalnie w tescie

Tylko override specyficzne dla danego testu, np.:

```java
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = "server.port=18081"
)
```
