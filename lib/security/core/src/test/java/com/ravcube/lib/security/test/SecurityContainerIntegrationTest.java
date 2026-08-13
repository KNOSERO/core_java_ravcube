package com.ravcube.lib.security.test;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static com.ravcube.test.keycloak.KeycloakTestProfiles.TEST_KEYCLOAK_PROFILE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles({"keycloak", TEST_KEYCLOAK_PROFILE})
@SpringBootTest(classes = SecurityTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityContainerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldAuthenticateByLoginApiAndExposeMappedSecurityContext() {
        ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/test/security/login",
                new LoginRequest("admin", "admin"),
                LoginResponse.class);

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertNotNull(loginResponse.getBody());
        assertNotNull(loginResponse.getBody().accessToken());
        assertFalse(loginResponse.getBody().accessToken().isBlank());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(loginResponse.getBody().accessToken());

        ResponseEntity<SecurityContextResponse> contextResponse = restTemplate.exchange(
                "http://localhost:" + port + "/test/security/context",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                SecurityContextResponse.class);

        assertEquals(HttpStatus.OK, contextResponse.getStatusCode());
        assertNotNull(contextResponse.getBody());
        assertNotNull(contextResponse.getBody().roles());
        assertFalse(contextResponse.getBody().roles().isEmpty());
        assertNotNull(contextResponse.getBody().claims());
        assertFalse(
                contextResponse.getBody().claims().isEmpty(),
                "Claims are empty: " + contextResponse.getBody().claims()
        );
    }

    @Test
    void shouldRejectAccessToProtectedContextEndpointWithoutToken() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/test/security/context",
                String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    private record LoginRequest(String username, String password) {
    }

    private record LoginResponse(String accessToken) {
    }

    private record SecurityContextResponse(List<String> roles, Map<String, Object> claims) {
    }
}
