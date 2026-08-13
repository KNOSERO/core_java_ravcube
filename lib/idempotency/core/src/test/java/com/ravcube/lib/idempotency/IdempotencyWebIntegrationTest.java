package com.ravcube.lib.idempotency;

import io.github.josipmusa.idempotency.core.IdempotencyStore;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static com.ravcube.lib.eureka.RavcubeEurekaClientExceptions.hasStatus;
import static com.ravcube.lib.eureka.RavcubeEurekaClientExceptions.status;
import static com.ravcube.test.eureka.EurekaTestProfiles.TEST_EUREKA_PROFILE;
import static com.ravcube.test.awaitility.Eventually.untilSucceeds;
import static com.ravcube.test.awaitility.Eventually.untilThrows;
import static com.ravcube.test.redis.RedisTestProfiles.TEST_REDIS_PROFILE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

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

        ResponseEntity<String> firstResponse = untilSucceeds(
                WAIT_TIMEOUT,
                RETRY_DELAY,
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
        ResponseEntity<String> firstResponse = untilSucceeds(
                WAIT_TIMEOUT,
                RETRY_DELAY,
                () -> shotClient.shot(UUID.randomUUID().toString(), Map.of("payload", "test"))
        );
        ResponseEntity<String> secondResponse = shotClient.shot(UUID.randomUUID().toString(), Map.of("payload", "test"));

        assertEquals(HttpStatus.OK, firstResponse.getStatusCode());
        assertEquals(HttpStatus.OK, secondResponse.getStatusCode());
        assertEquals(2, shotController.invocations());
    }

    @Test
    void shouldRejectIdempotentEndpointWithoutIdempotencyKey() {
        RuntimeException exception = untilThrows(
                WAIT_TIMEOUT,
                RETRY_DELAY,
                RuntimeException.class,
                () -> shotClient.shotWithoutIdempotencyKey(Map.of("payload", "test")),
                clientException -> hasStatus(clientException, 422)
        );

        assertEquals(422, status(exception).orElseThrow());
        assertEquals(0, shotController.invocations());
    }

    @Test
    void shouldRejectSameIdempotencyKeyWithDifferentPayload() {
        String idempotencyKey = UUID.randomUUID().toString();

        ResponseEntity<String> firstResponse = untilSucceeds(
                WAIT_TIMEOUT,
                RETRY_DELAY,
                () -> shotClient.shot(idempotencyKey, Map.of("payload", "first"))
        );
        RuntimeException exception = untilThrows(
                WAIT_TIMEOUT,
                RETRY_DELAY,
                RuntimeException.class,
                () -> shotClient.shot(idempotencyKey, Map.of("payload", "second")),
                clientException -> hasStatus(clientException, 422)
        );

        assertEquals(HttpStatus.OK, firstResponse.getStatusCode());
        assertEquals(422, status(exception).orElseThrow());
        assertEquals(1, shotController.invocations());
    }

    @Test
    void shouldExecuteOnlyOnceWhenSameIdempotencyKeyIsSentConcurrently() throws Exception {
        untilSucceeds(
                WAIT_TIMEOUT,
                RETRY_DELAY,
                () -> shotClient.shot(UUID.randomUUID().toString(), Map.of("payload", "warm-up"))
        );
        shotController.reset();

        String idempotencyKey = UUID.randomUUID().toString();
        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Callable<ShotResult> shot = () -> {
                startLatch.await();
                return callSlowShot(idempotencyKey, Map.of("payload", "test"));
            };
            List<Future<ShotResult>> futures = List.of(executor.submit(shot), executor.submit(shot));

            startLatch.countDown();

            List<ShotResult> results = List.of(futures.get(0).get(), futures.get(1).get());

            assertEquals(1, results.stream().filter(ShotResult::isOk).count());
            assertEquals(1, results.stream().filter(result -> result.hasStatus(503)).count());
            assertEquals(1, shotController.invocations());
        } finally {
            executor.shutdownNow();
        }
    }

    private ShotResult callSlowShot(String idempotencyKey, Map<String, String> payload) {
        try {
            ResponseEntity<String> response = shotClient.slowShot(idempotencyKey, true, payload);
            return new ShotResult(response.getStatusCode().value());
        } catch (RuntimeException exception) {
            return new ShotResult(status(exception).orElseThrow());
        }
    }

    private record ShotResult(int status) {

        boolean isOk() {
            return hasStatus(200);
        }

        boolean hasStatus(int expectedStatus) {
            return status == expectedStatus;
        }
    }
}
