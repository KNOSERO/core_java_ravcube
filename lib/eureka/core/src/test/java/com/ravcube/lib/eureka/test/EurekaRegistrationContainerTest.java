package com.ravcube.lib.eureka.test;

import com.ravcube.lib.eureka.client.TestClient;
import com.ravcube.lib.eureka.config.EurekaCoreTestApplication;
import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.ravcube.test.awaitility.Eventually.untilSucceeds;
import static com.ravcube.test.eureka.EurekaTestProfiles.TEST_EUREKA_PROFILE;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles({"eureka", TEST_EUREKA_PROFILE})
@SpringBootTest(
        classes = EurekaCoreTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "ravcube.testcontainers.eureka.enabled=false",
                "eureka.client.enabled=false"
        }
)
class EurekaRegistrationContainerTest {

    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration RETRY_DELAY = Duration.ofMillis(250);
    private static final int SERVICE_PORT = randomAvailablePort();

    @Autowired
    private TestClient testClient;

    @DynamicPropertySource
    static void registerServicePort(DynamicPropertyRegistry registry) {
        registry.add("server.port", () -> SERVICE_PORT);
    }

    @Test
    void shouldCallServiceThroughEurekaUsingTestClient() {
        String response = untilSucceeds(WAIT_TIMEOUT, RETRY_DELAY, testClient::ping);
        assertEquals("pong", response);
    }

    private static int randomAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot allocate random service port for test", exception);
        }
    }
}
