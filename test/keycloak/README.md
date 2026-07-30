# test:keycloak

Instrukcja tworzenia nowych modulow `test:*` jest opisana w [test/common/README.md](../common/README.md).

Ten modul sluzy do podpiecia testowego kontenera Keycloak w innych modulach.

## Jak zainstalowac do testow

Dodaj zaleznosc:

```kotlin
testImplementation(project(":test:keycloak"))
```

## Jak uzyc w tescie modulu docelowego

Aktywuj profil bazowy i testowy:

```java
import static com.ravcube.test.keycloak.KeycloakTestProfiles.TEST_KEYCLOAK_PROFILE;

@ActiveProfiles({"keycloak", TEST_KEYCLOAK_PROFILE})
```

## Co modul ustawia automatycznie

- start kontenera Keycloak
- `spring.security.oauth2.resourceserver.jwt.issuer-uri`

## Czego nie wpisywac recznie w module docelowym

Nie duplikuj w testach:

- `spring.security.oauth2.resourceserver.jwt.issuer-uri`
- `ravcube.testcontainers.keycloak.image`
- `ravcube.testcontainers.keycloak.realm`
