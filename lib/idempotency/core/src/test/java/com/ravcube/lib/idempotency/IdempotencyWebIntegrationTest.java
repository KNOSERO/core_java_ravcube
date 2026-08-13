package com.ravcube.lib.idempotency;

import feign.FeignException;
import io.github.josipmusa.idempotency.core.IdempotencyStore;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static com.ravcube.test.eureka.EurekaTestProfiles.TEST_EUREKA_PROFILE;
import static com.ravcube.test.redis.RedisTestProfiles.TEST_REDIS_PROFILE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles({"redis", TEST_REDIS_PROFILE, "eureka", TEST_EUREKA_PROFILE})
@SpringBootTest(
        classes = IdempotencyWebTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "server.port=18081"
        }
)
class IdempotencyWebIntegrationTest {

    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration RETRY_DELAY = Duration.ofMillis(250);

    @Autowired
    private IdempotencyShotClient shotClient;

    @Autowired
    private IdempotencyShotController shotController;

    @Autowired
    private IdempotencyStore idempotencyStore;

    @BeforeEach
    void beforeEach() {
        shotController.reset();
    }

    @Test
    void shouldReplayFirstResponseWhenSameShotIsSentTwice() {
        assertInstanceOf(CacheStoreIdempotencyStore.class, idempotencyStore);
        String idempotencyKey = UUID.randomUUID().toString();

        ResponseEntity<String> firstResponse = waitUntilSuccess(
                () -> shotClient.shot(idempotencyKey, Map.of("payload", "test"))
        );
        ResponseEntity<String> replayedResponse = shotClient.shot(idempotencyKey, Map.of("payload", "test"));

        assertEquals(HttpStatus.OK, firstResponse.getStatusCode());
        assertEquals(HttpStatus.OK, replayedResponse.getStatusCode());
        assertEquals(firstResponse.getBody(), replayedResponse.getBody());
        assertEquals("true", replayedResponse.getHeaders().getFirst("Idempotent-Replayed"));
        assertEquals(1, shotController.invocations());
    }

    @Test
    void shouldExecuteAgainWhenIdempotencyKeyIsDifferent() {
        ResponseEntity<String> firstResponse = waitUntilSuccess(
                () -> shotClient.shot(UUID.randomUUID().toString(), Map.of("payload", "test"))
        );
        ResponseEntity<String> secondResponse = shotClient.shot(UUID.randomUUID().toString(), Map.of("payload", "test"));

        assertEquals(HttpStatus.OK, firstResponse.getStatusCode());
        assertEquals(HttpStatus.OK, secondResponse.getStatusCode());
        assertEquals(2, shotController.invocations());
    }

    @Test
    void shouldRejectIdempotentEndpointWithoutIdempotencyKey() {
        FeignException exception = waitUntilExpectedFeignException(
                () -> shotClient.shotWithoutIdempotencyKey(Map.of("payload", "test")),
                422
        );

        assertEquals(422, exception.status());
        assertEquals(0, shotController.invocations());
    }

    private <T> T waitUntilSuccess(Supplier<T> supplier) {
        Instant deadline = Instant.now().plus(WAIT_TIMEOUT);
        Throwable lastFailure = null;

        while (Instant.now().isBefore(deadline)) {
            try {
                return supplier.get();
            } catch (Throwable exception) {
                lastFailure = exception;
                sleep(RETRY_DELAY);
            }
        }

        throw new IllegalStateException("Condition was not met before timeout", lastFailure);
    }

    private FeignException waitUntilExpectedFeignException(Runnable runnable, int expectedStatus) {
        Instant deadline = Instant.now().plus(WAIT_TIMEOUT);
        Throwable lastFailure = null;

        while (Instant.now().isBefore(deadline)) {
            try {
                FeignException exception = assertThrows(FeignException.class, runnable::run);
                if (exception.status() == expectedStatus) {
                    return exception;
                }
                lastFailure = exception;
            } catch (Throwable exception) {
                lastFailure = exception;
            }
            sleep(RETRY_DELAY);
        }

        throw new IllegalStateException("Expected Feign status was not returned before timeout", lastFailure);
    }

    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting", interruptedException);
        }
    }
}
