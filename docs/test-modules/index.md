# Test Modules

The `test:*` modules provide reusable integration-test infrastructure. They keep
test classes focused on behavior instead of container setup.

## Standard usage pattern

1. Add the matching `test:*` dependency.
2. Import the profile constant.
3. Activate the production profile and the test profile.
4. Let the test module provide connection properties.

Example:

```java
import static com.ravcube.test.redis.RedisTestProfiles.TEST_REDIS_PROFILE;

@SpringBootTest
@ActiveProfiles({"redis", TEST_REDIS_PROFILE})
class RedisBackedCacheTest {
}
```

## Usage Rules

Do not hardcode profile names like `"test-redis"` in every class. Use the
public constants from the test module.

Do not copy host, port, broker, URL, or credential properties into tests unless
the test specifically verifies a custom override.

Most container modules can be disabled with an
`ravcube.testcontainers.<name>.enabled=false` property. Use this only when a test
intentionally connects to an externally managed service.

Image overrides are module-specific and documented on each module page. A shared
container pins its image for the current JVM; do not request different images
for the same module in one test run.

## When not to use a test module

If the behavior can be tested without a real external dependency, write a unit
or integration test without a container. Containers are slower and should prove
real integration behavior.
