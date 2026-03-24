# test:postgresql

Centralna konfiguracja testowa PostgreSQL oparta o Testcontainers.
Po dodaniu jednej zależności dostajesz gotowy `spring.datasource.*` i zestaw podstawowych zależności testowych.

## Co daje modul

- Domyslna konfiguracja datasource przez Testcontainers (`jdbc:tc:postgresql:...`)
- Domyslne ustawienia Postgresa (version/database/username/password)
- `spring.jpa.hibernate.ddl-auto=create-drop`
- Zaleznosci testowe:
  - `spring-boot-starter-test`
  - `testcontainers-postgresql`
  - `org.postgresql:postgresql` (runtime)
  - `junit-platform-launcher` (runtime)

## Jak uzyc

Dodaj jedna zaleznosc testowa w module, ktory ma odpalac testy:

```kotlin
testImplementation(project(":test:postgresql"))
```

Nie trzeba dodawac `spring.config.import`.
Konfiguracja laduje sie automatycznie z `application.yml` tego modulu.

## Domyslna konfiguracja

Plik konfiguracyjny:

- `application.yml` (w module `:test:postgresql`)

Domyslne wartosci:

- `ravcube.testcontainers.postgres.version=18-alpine`
- `ravcube.testcontainers.postgres.database=test`
- `ravcube.testcontainers.postgres.username=test`
- `ravcube.testcontainers.postgres.password=test`

Wlasciwosci do nadpisania:

- `ravcube.testcontainers.postgres.version`
- `ravcube.testcontainers.postgres.database`
- `ravcube.testcontainers.postgres.username`
- `ravcube.testcontainers.postgres.password`

Konfigurowany URL datasource:

```yaml
spring:
  datasource:
    url: jdbc:tc:postgresql:${ravcube.testcontainers.postgres.version}:///${ravcube.testcontainers.postgres.database}
```

## Nadpisywanie w module testowanym

W `application-test.yml` nadpisz tylko to, co chcesz zmienic:

```yaml
ravcube:
  testcontainers:
    postgres:
      username: user
      password: my-secret
```

Przyklad zmiany wersji i bazy:

```yaml
ravcube:
  testcontainers:
    postgres:
      version: 18.3-alpine
      database: my_test_db
```

## Uruchamianie

Przyklad uruchomienia testow modulu:

```powershell
.\gradlew.bat :lib:data:test
```
