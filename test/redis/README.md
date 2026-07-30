# test:redis

Wzor tworzenia modulow `test:*` jest opisany w [test/common/README.md](../common/README.md).

Ten modul dostarcza gotowy kontener Redis do uzycia w testach innych modulow.

## Jak dodac do testow

Dodaj zaleznosc:

```kotlin
testImplementation(project(":test:redis"))
```

Aktywuj profile:

```java
import static com.ravcube.test.redis.RedisTestProfiles.TEST_REDIS_PROFILE;

@ActiveProfiles({"redis", TEST_REDIS_PROFILE})
```

## Co dostajesz z modulu

- start wspolnego kontenera Redis
- automatyczne ustawienie:
  - `spring.data.redis.host`
  - `spring.data.redis.port`

## Czego nie kopiowac do testu

Nie ustawiaj recznie:

- `spring.data.redis.host`
- `spring.data.redis.port`

To ma byc utrzymywane w tym module.
