package com.ravcube.lib.stream.test;

import com.ravcube.lib.stream.test.support.ClientStreamApiTestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;

import static com.ravcube.test.kafka.KafkaTestProfiles.TEST_KAFKA_PROFILE;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles({"stream", "stream-test", "kafka", TEST_KAFKA_PROFILE})
@SpringBootTest(
        classes = ClientStreamApiTestApplication.class,
        properties = {
                "spring.application.name=stream-api-capacity-test",
                "ravcube.stream.kafka.instance-id=capacity-test-pod",
                "ravcube.stream.max-subscriptions-per-client=1"
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ClientStreamApiCapacityIntegrationTest {

    @LocalServerPort
    private int port;

    @Test
    void secondSubscriptionFromSameClientReturnsTooManyRequests() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest firstRequest = request("/streams/claims/1");

        HttpResponse<InputStream> first = client
                .sendAsync(firstRequest, HttpResponse.BodyHandlers.ofInputStream())
                .get(10, TimeUnit.SECONDS);
        try (InputStream ignored = first.body()) {
            HttpRequest secondRequest = request("/streams/claims/2");
            HttpResponse<String> second = client.send(
                    secondRequest,
                    HttpResponse.BodyHandlers.ofString()
            );

            assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), second.statusCode());
        }
    }

    private HttpRequest request(String path) {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header(HttpHeaders.ACCEPT, "text/event-stream")
                .GET()
                .build();
    }
}
