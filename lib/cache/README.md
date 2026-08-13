# lib:cache

Moduly cache daja proste API do pracy z cache oraz implementacje Redis dla Spring.

## Moduly

- `lib:cache:api` - kontrakt `CacheStore`
- `lib:cache:core` - implementacja `RedisCacheStore` oraz konfiguracja Spring
- `test:redis` - testowy Redis oparty o Testcontainers

## Jak uzyc

Dodaj zaleznosc w module aplikacji:

```kotlin
implementation(project(":lib:cache:api"))
implementation(project(":lib:cache:core"))
```

Wlacz profil `redis`:

```yaml
spring:
  profiles:
    active: redis
```

Albo w testach:

```java
import static com.ravcube.test.redis.RedisTestProfiles.TEST_REDIS_PROFILE;

@ActiveProfiles({"redis", TEST_REDIS_PROFILE})
```

## Przyklad serwisu Spring

```java
@Service
class UserSessionCache {

    private final CacheStore cacheStore;

    UserSessionCache(CacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }

    void save(UserSession session) {
        cacheStore.put("user-session:" + session.id(), session, Duration.ofMinutes(15));
    }

    Optional<UserSession> find(String id) {
        return cacheStore.get("user-session:" + id, UserSession.class);
    }
}
```

Obiekty zapisywane przez aktualna konfiguracje Redis musza implementowac `Serializable`,
bo `RedisCacheStore` uzywa `JdkSerializationRedisSerializer`.

## Test integracyjny

W module testowanym dodaj:

```kotlin
testImplementation(project(":test:redis"))
```

Nastepnie uruchom test z profilami:

```java
import static com.ravcube.test.redis.RedisTestProfiles.TEST_REDIS_PROFILE;

@ActiveProfiles({"redis", TEST_REDIS_PROFILE})
@SpringBootTest
class RedisUsageTest {
}
```

`test:redis` uruchomi kontener Redis i ustawi:

- `ravcube.redis.host`
- `ravcube.redis.port`

## Konfiguracja

Domyslne ustawienia produkcyjne sa w `lib/cache/core/src/main/resources/application-redis.yml`.

Najwazniejsze zmienne:

- `RAVCUBE_REDIS_HOST`
- `RAVCUBE_REDIS_PORT`
- `RAVCUBE_REDIS_DATABASE`
- `RAVCUBE_REDIS_USERNAME`
- `RAVCUBE_REDIS_PASSWORD`
- `RAVCUBE_REDIS_TIMEOUT`
