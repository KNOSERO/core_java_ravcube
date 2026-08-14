package com.ravcube.lib.faulttolerance.integration;

import com.ravcube.lib.faulttolerance.support.FaultToleranceTestApplication;
import com.ravcube.lib.faulttolerance.support.TestHttpServer;
import com.ravcube.lib.faulttolerance.support.TestResponse;
import com.ravcube.lib.faulttolerance.support.UnavailableServiceClient;
import com.ravcube.lib.faulttolerance.support.UnavailableServiceFallback;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.ravcube.test.awaitility.Eventually.untilAsserted;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles({"fault-tolerance", "test-fault-tolerance"})
@SpringBootTest(
        classes = FaultToleranceTestApplication.class
)
class OpenFeignCircuitBreakerIntegrationTest {

    private static final int UNAVAILABLE_SERVICE_PORT = randomAvailablePort();

    @Autowired
    private UnavailableServiceClient client;

    @Autowired
    private UnavailableServiceFallback fallback;

    @Autowired
    private CircuitBreakerRegistry circuitBreakers;

    @DynamicPropertySource
    static void unavailableServicePort(DynamicPropertyRegistry registry) {
        registry.add("ravcube.test.unavailable-service.port", () -> UNAVAILABLE_SERVICE_PORT);
    }

    @BeforeEach
    void resetCircuitBreaker() {
        fallback.reset();
        circuitBreakers.getAllCircuitBreakers().forEach(CircuitBreaker::reset);
    }

    @Test
    void shouldOpenCircuitBreakerWhenDiscoveredServiceIsUnavailable() {
        callUnavailableServiceUntilCircuitOpens();

        assertEquals(3, fallback.invocations());
        assertEquals(1, openCircuitBreakers());
    }

    @Test
    void shouldFailFastWithoutHttpRequestWhenCircuitBreakerIsOpen() throws IOException {
        callUnavailableServiceUntilCircuitOpens();

        try (TestHttpServer service = TestHttpServer.start(UNAVAILABLE_SERVICE_PORT, TestResponse.ok("available"))) {
            assertEquals("fallback", client.status());

            assertEquals(0, service.requests());
            assertEquals(4, fallback.invocations());
            assertEquals(1, openCircuitBreakers());
        }
    }

    @Test
    void shouldRecoverAndCloseCircuitBreakerWhenServiceReturns() throws IOException {
        callUnavailableServiceUntilCircuitOpens();

        try (TestHttpServer service = TestHttpServer.start(UNAVAILABLE_SERVICE_PORT, TestResponse.ok("available"))) {
            untilAsserted(
                    Duration.ofSeconds(3),
                    Duration.ofMillis(50),
                    () -> assertEquals("available", client.status())
            );
            untilAsserted(
                    Duration.ofSeconds(1),
                    Duration.ofMillis(10),
                    () -> assertEquals(0, openCircuitBreakers())
            );

            assertTrue(service.requests() > 0);
        }
    }

    @Test
    void shouldUseSingleHttpRequestWhenServiceReturnsServerError() throws IOException {
        try (TestHttpServer service = TestHttpServer.start(UNAVAILABLE_SERVICE_PORT, TestResponse.error(503))) {
            assertEquals("fallback", client.status());

            assertEquals(1, service.requests());
            assertEquals(1, fallback.invocations());
        }
    }

    @Test
    void shouldUseFallbackWhenServiceAcceptsConnectionButDoesNotRespondInTime() throws IOException {
        try (TestHttpServer service = TestHttpServer.start(
                UNAVAILABLE_SERVICE_PORT,
                TestResponse.slow(Duration.ofMillis(500), "too-late")
        )) {
            assertEquals("fallback", client.status());

            assertEquals(1, service.requests());
            assertEquals(1, fallback.invocations());
        }
    }

    private void callUnavailableServiceUntilCircuitOpens() {
        assertEquals("fallback", client.status());
        assertEquals("fallback", client.status());
        assertEquals("fallback", client.status());
        untilAsserted(
                Duration.ofSeconds(1),
                Duration.ofMillis(10),
                () -> assertEquals(1, openCircuitBreakers())
        );
    }

    private long openCircuitBreakers() {
        return circuitBreakers.getAllCircuitBreakers()
                .stream()
                .map(CircuitBreaker::getState)
                .filter(CircuitBreaker.State.OPEN::equals)
                .count();
    }

    private static int randomAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot allocate unavailable service port for test", exception);
        }
    }
}
