# Redis Test Module

Module:

```text
test:redis
```

Use `test:redis` when a test needs real Redis behavior.

## Dependency

```kotlin
dependencies {
    testImplementation(project(":test:redis"))
}
```

## Activate profiles

```java
import static com.ravcube.test.redis.RedisTestProfiles.TEST_REDIS_PROFILE;

@SpringBootTest
@ActiveProfiles({"redis", TEST_REDIS_PROFILE})
class RedisCacheIntegrationTest {
}
```

## What this module configures

The module starts Redis and provides Spring Redis connection properties. Tests
should not manually set Redis host or port.

Injected properties:

| Property | Value |
| --- | --- |
| `ravcube.redis.host` | Testcontainer host. |
| `ravcube.redis.port` | Mapped Redis port. |

Override properties:

| Property | Default |
| --- | --- |
| `ravcube.testcontainers.redis.enabled` | `true` |
| `ravcube.testcontainers.redis.image` | `redis:7.2-alpine` |

The production `redis` profile still supplies default database, username,
password, and timeout values through `application-test-redis.yml`.

## Good use cases

- TTL expiration,
- Redis serialization,
- atomic `putIfAbsent`,
- atomic replace behavior,
- integration with `lib:cache:core` or `lib:idempotency:core`.
