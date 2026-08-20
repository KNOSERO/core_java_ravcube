# Getting Started

This project is a Gradle multi-module repository. Production code lives under
`lib:*`. Reusable test infrastructure lives under `test:*`.

## Basic workflow

1. Choose the module that already owns the technical capability.
2. Add a Gradle dependency on that module.
3. Enable the required Spring profile, if the module is profile-based.
4. Use the public API shown in the documentation.
5. Use matching `test:*` modules for container-based integration tests.

## Example

To use Redis-backed cache in an application module:

```kotlin
dependencies {
    implementation(project(":lib:cache:api"))
    implementation(project(":lib:cache:core"))

    testImplementation(project(":test:redis"))
}
```

Then enable profiles in tests:

```java
import static com.ravcube.test.redis.RedisTestProfiles.TEST_REDIS_PROFILE;

@ActiveProfiles({"redis", TEST_REDIS_PROFILE})
@SpringBootTest
class RedisCacheIntegrationTest {
}
```

## What not to do

Do not copy container setup, Redis host properties, Kafka broker URLs, or
Elasticsearch URLs into every test. The `test:*` modules exist to centralize
that work.
