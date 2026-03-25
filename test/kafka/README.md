# test:kafka

Centralna konfiguracja testowa Kafka oparta o Testcontainers.
Po dodaniu jednej zaleznosci dostajesz automatycznie uruchamiany kontener Kafka
i ustawione `spring.kafka.bootstrap-servers`.

## Co daje modul

- Start wspolnego kontenera Kafka dla testowego kontekstu Spring
- Automatyczne ustawienie `spring.kafka.bootstrap-servers`
- Zaleznosci testowe:
  - `spring-boot-starter-test`
  - `spring-kafka`
  - `testcontainers-kafka`
  - `junit-platform-launcher` (runtime)

## Jak uzyc

Dodaj jedna zaleznosc testowa w module, ktory ma odpalac testy:

```kotlin
testImplementation(project(":test:kafka"))
```

Nie trzeba dodawac `spring.config.import`.
Konfiguracja laduje sie automatycznie.

## Domyslna konfiguracja

Plik konfiguracyjny:

- `application.yml` (w module `:test:kafka`)

Domyslne wartosci:

- `ravcube.testcontainers.kafka.enabled=true`
- `ravcube.testcontainers.kafka.image=apache/kafka-native:3.9.0`

Wlasciwosci do nadpisania:

- `ravcube.testcontainers.kafka.enabled`
- `ravcube.testcontainers.kafka.image`

## Nadpisywanie w module testowanym

W `application-test.yml` nadpisz tylko to, co chcesz zmienic:

```yaml
ravcube:
  testcontainers:
    kafka:
      image: apache/kafka-native:3.9.1
```

## Uruchamianie

Przyklad uruchomienia testow modulu:

```powershell
.\gradlew.bat :test:kafka:build
```
