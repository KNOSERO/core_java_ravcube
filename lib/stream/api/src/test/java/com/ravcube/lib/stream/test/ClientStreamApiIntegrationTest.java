package com.ravcube.lib.stream.test;

import com.ravcube.lib.event.api.EventPublisher;
import com.ravcube.lib.stream.common.event.ClientStreamRefreshEvent;
import com.ravcube.lib.stream.test.support.ClientStreamApiTestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.ravcube.test.kafka.KafkaTestProfiles.TEST_KAFKA_PROFILE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles({"stream", "stream-test", "kafka", TEST_KAFKA_PROFILE})
@SpringBootTest(
        classes = ClientStreamApiTestApplication.class,
        properties = {
                "spring.application.name=stream-api-test",
                "ravcube.stream.kafka.instance-id=test-pod"
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ClientStreamApiIntegrationTest {

    private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

    @LocalServerPort
    private int port;

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void subscriptionOpensHttpSseStream() throws Exception {
        try (SseConnection connection = openStream("/streams/claims?ids=1&ids=2")) {
            assertEquals(HttpStatus.OK.value(), connection.status());
            assertTrue(connection.contentType().startsWith("text/event-stream"));
        }
    }

    @Test
    void refreshEventReachesSubscribedSseClientThroughKafka() throws Exception {
        try (SseConnection connection = openStream("/streams/claims?ids=1")) {
            publishRefresh("claims", "1", 42);

            String event = connection.readUntil("\"version\":42");

            assertTrue(event.contains("event:refresh"));
            assertTrue(event.contains(
                    "data:{\"resourceId\":\"1\",\"version\":42}"
            ));
        }
    }

    @Test
    void refreshIsRoutedOnlyToMatchingSseClient() throws Exception {
        try (
                SseConnection subscribed = openStream("/streams/claims?ids=1");
                SseConnection other = openStream("/streams/claims?ids=2")
        ) {
            publishRefresh("claims", "1", 42);

            assertTrue(subscribed.readUntil("\"version\":42").contains("\"resourceId\":\"1\""));
            assertThrows(
                    TimeoutException.class,
                    () -> other.readUntil("\"version\":42", Duration.ofSeconds(2))
            );
        }
    }

    @Test
    void rollbackDoesNotPublishRefresh() throws Exception {
        try (SseConnection connection = openStream("/streams/claims?ids=1")) {
            transactionTemplate.executeWithoutResult(status -> {
                eventPublisher.publish(new ClientStreamRefreshEvent("claims", "1", 42));
                status.setRollbackOnly();
            });

            assertThrows(
                    TimeoutException.class,
                    () -> connection.readUntil("\"version\":42", Duration.ofSeconds(2))
            );
        }
    }

    @Test
    void refreshOutsideTransactionIsRejected() {
        assertThrows(
                IllegalStateException.class,
                () -> eventPublisher.publish(new ClientStreamRefreshEvent("claims", "1", 42))
        );
    }

    @Test
    void invalidSubscriptionReturnsBadRequest() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(uri("/streams/claims"))
                .header(HttpHeaders.ACCEPT, "text/event-stream")
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode());
    }

    private void publishRefresh(String resourceName, String resourceId, long version) {
        transactionTemplate.executeWithoutResult(status ->
                eventPublisher.publish(new ClientStreamRefreshEvent(resourceName, resourceId, version))
        );
    }

    private SseConnection openStream(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(uri(path))
                .header(HttpHeaders.ACCEPT, "text/event-stream")
                .GET()
                .build();
        HttpResponse<InputStream> response = HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .get(READ_TIMEOUT.toSeconds(), TimeUnit.SECONDS);

        return new SseConnection(response);
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private static final class SseConnection implements AutoCloseable {

        private final HttpResponse<InputStream> response;
        private final ExecutorService readerExecutor = Executors.newSingleThreadExecutor();

        private SseConnection(HttpResponse<InputStream> response) {
            this.response = response;
        }

        private int status() {
            return response.statusCode();
        }

        private String contentType() {
            return response.headers().firstValue(HttpHeaders.CONTENT_TYPE).orElse("");
        }

        private String readUntil(String expected) throws Exception {
            return readUntil(expected, READ_TIMEOUT);
        }

        private String readUntil(String expected, Duration timeout) throws Exception {
            Future<String> read = readerExecutor.submit(() -> readUntilBlocking(expected));
            try {
                return read.get(timeout.toSeconds(), TimeUnit.SECONDS);
            } catch (TimeoutException | ExecutionException exception) {
                response.body().close();
                read.cancel(true);
                throw exception;
            }
        }

        private String readUntilBlocking(String expected) throws IOException {
            StringBuilder content = new StringBuilder();
            byte[] buffer = new byte[256];
            int read;
            while ((read = response.body().read(buffer)) >= 0) {
                content.append(new String(buffer, 0, read));
                if (content.toString().contains(expected)) {
                    return content.toString();
                }
            }
            throw new IOException("SSE stream closed before receiving: " + expected);
        }

        @Override
        public void close() throws Exception {
            response.body().close();
            readerExecutor.shutdownNow();
            readerExecutor.awaitTermination(READ_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
        }
    }
}
