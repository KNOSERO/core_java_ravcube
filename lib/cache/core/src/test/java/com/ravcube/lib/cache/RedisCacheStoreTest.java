package com.ravcube.lib.cache;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.Serializable;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static com.ravcube.test.redis.RedisTestProfiles.TEST_REDIS_PROFILE;
import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles({"redis", TEST_REDIS_PROFILE})
@SpringBootTest(classes = TestApplication.class)
class RedisCacheStoreTest {

    @Autowired
    private CacheStore cacheStore;

    @Test
    void shouldStoreAndReadTypedValue() {
        // given
        String key = "cache:test:" + UUID.randomUUID();
        SamplePayload payload = new SamplePayload("alpha", 7);
        cacheStore.put(key, payload);

        // when
        Optional<SamplePayload> result = cacheStore.get(key, SamplePayload.class);

        // then
        assertTrue(result.isPresent());
        assertEquals(payload, result.get());
    }

    @Test
    void shouldExpireValueAfterTtl() throws InterruptedException {
        // given
        String key = "cache:test:" + UUID.randomUUID();
        cacheStore.put(key, "short-lived", Duration.ofMillis(300));

        // when
        waitUntilMissing(key, Duration.ofSeconds(3));

        // then
        assertTrue(cacheStore.get(key, String.class).isEmpty());
    }

    @Test
    void shouldDeleteValueByKey() {
        // given
        String key = "cache:test:" + UUID.randomUUID();
        cacheStore.put(key, "value");

        // when
        cacheStore.delete(key);

        // then
        assertTrue(cacheStore.get(key, String.class).isEmpty());
    }

    @Test
    void shouldPutValueOnlyWhenKeyIsAbsent() {
        // given
        String key = "cache:test:" + UUID.randomUUID();

        // when
        boolean firstPut = cacheStore.putIfAbsent(key, "first", Duration.ofMinutes(1));
        boolean secondPut = cacheStore.putIfAbsent(key, "second", Duration.ofMinutes(1));

        // then
        assertTrue(firstPut);
        assertFalse(secondPut);
        assertEquals("first", cacheStore.get(key, String.class).orElseThrow());
    }

    @Test
    void shouldReplaceValueOnlyWhenExpectedValueMatches() {
        // given
        String key = "cache:test:" + UUID.randomUUID();
        cacheStore.put(key, "first", Duration.ofMinutes(1));

        // when
        boolean rejected = cacheStore.replace(key, "other", "rejected", Duration.ofMinutes(1));
        boolean replaced = cacheStore.replace(key, "first", "second", Duration.ofMinutes(1));

        // then
        assertFalse(rejected);
        assertTrue(replaced);
        assertEquals("second", cacheStore.get(key, String.class).orElseThrow());
    }

    private void waitUntilMissing(String key, Duration timeout) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            if (cacheStore.get(key, String.class).isEmpty()) {
                return;
            }
            sleep(50);
        }
    }

    private record SamplePayload(String name, int version) implements Serializable {
    }
}
