# Cache

Modules:

```text
lib:cache:api
lib:cache:core
```

The cache library provides a small storage abstraction and concrete
implementations for memory and Redis.

## Responsibility

`CacheStore` is the stable contract. Code that only needs cache behavior should
depend on `lib:cache:api`, not directly on Redis.

`lib:cache:core` provides two implementations:

| Profile | Bean | Behavior |
| --- | --- | --- |
| no `redis` profile | `DefaultCacheStore` | In-memory synchronized map with local TTL checks. |
| `redis` | `RedisCacheStore` | Redis-backed store using `RedisTemplate<String, Object>`. |

## Dependency

```kotlin
dependencies {
    implementation(project(":lib:cache:api"))
    implementation(project(":lib:cache:core"))
}
```

## Example service

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

## Operations

| Operation | Use when |
| --- | --- |
| `get` | Read a typed value. |
| `put` | Store a value. |
| `put` with TTL | Store a temporary value. |
| `putIfAbsent` | Acquire a lightweight lock or first-writer-wins value. |
| `replace` | Update only when the current value is known. |
| `delete` | Remove a value. |

## Configuration

Enable Redis-backed cache with the `redis` Spring profile. Without that profile,
the module provides an in-memory `CacheStore`.

```yaml
spring:
  profiles:
    active: redis
```

The Redis profile reads these properties:

| Property | Purpose |
| --- | --- |
| `ravcube.redis.host` | Redis host. |
| `ravcube.redis.port` | Redis port. |
| `ravcube.redis.database` | Redis database index. |
| `ravcube.redis.username` | Redis username. |
| `ravcube.redis.password` | Redis password. |
| `ravcube.redis.timeout` | Spring Redis connection timeout. |

`RedisCacheStore` uses JDK serialization for values. Cached values therefore
must be serializable when stored in Redis.

## Contract

- Keys, values, value types, TTL values, and compare-and-replace values must not
  be `null`.
- `get` returns `Optional.empty()` for missing or expired values.
- `get` throws `IllegalStateException` when a stored value is not assignable to
  the requested type.
- `putIfAbsent` stores only when the key is currently missing.
- `replace` stores only when the current value equals the expected value.
- Redis `replace` uses Redis watch/multi/exec semantics for optimistic
  concurrency.

## Design warning

Do not create domain-specific Redis wrappers when `CacheStore` already solves
the storage problem. Put domain naming in the service that uses cache.
