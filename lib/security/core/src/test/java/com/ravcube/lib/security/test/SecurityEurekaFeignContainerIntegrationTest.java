package com.ravcube.lib.security.test;

import com.ravcube.lib.security.auth.api.LoginRequest;
import com.ravcube.lib.security.auth.api.RefreshTokenRequest;
import com.ravcube.lib.security.auth.api.TokenResponse;
import com.ravcube.lib.security.test.support.SecurityApiScenarioClient;
import com.ravcube.lib.security.test.support.SecurityContextResponse;
import com.ravcube.lib.security.test.support.SecurityEurekaFeignTestApplication;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.ravcube.lib.security.test.support.TestPorts.randomAvailablePort;
import static com.ravcube.test.eureka.EurekaTestProfiles.TEST_EUREKA_PROFILE;
import static com.ravcube.test.keycloak.KeycloakTestProfiles.TEST_KEYCLOAK_PROFILE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles({"keycloak", TEST_KEYCLOAK_PROFILE, "eureka", TEST_EUREKA_PROFILE})
@SpringBootTest(
        classes = SecurityEurekaFeignTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = "spring.autoconfigure.exclude=com.ravcube.test.eureka.EurekaClientTestConfiguration"
)
class SecurityEurekaFeignContainerIntegrationTest {

    private static final int SERVICE_PORT = randomAvailablePort();

    @Autowired
    private SecurityApiScenarioClient securityApi;

    @DynamicPropertySource
    static void registerServicePort(DynamicPropertyRegistry registry) {
        registry.add("server.port", () -> SERVICE_PORT);
    }

    @Test
    void shouldSupportLoginContextRefreshAndLogoutThroughEurekaFeign() {
        TokenResponse loginResponse = securityApi.login("admin", "admin");
        assertTokenResponse(loginResponse);

        SecurityContextResponse contextResponse = securityApi.context(loginResponse.accessToken());
        assertFalse(contextResponse.roles().isEmpty());
        assertFalse(contextResponse.claims().isEmpty());

        TokenResponse refreshedResponse = securityApi.refresh(loginResponse.refreshToken());
        assertTokenResponse(refreshedResponse);
        assertNotEquals(loginResponse.refreshToken(), refreshedResponse.refreshToken());

        securityApi.logout(refreshedResponse.refreshToken());

        FeignException refreshAfterLogout = securityApi.expectStatus(
                400,
                () -> securityApi.authClient().refresh(new RefreshTokenRequest(refreshedResponse.refreshToken()))
        );
        assertEquals(400, refreshAfterLogout.status());
    }

    @Test
    void shouldRejectInvalidLoginThroughEurekaFeign() {
        FeignException exception = securityApi.expectStatus(
                401,
                () -> securityApi.authClient().login(new LoginRequest("admin", "wrong"))
        );

        assertEquals(401, exception.status());
    }

    @Test
    void shouldRejectProtectedContextWithoutTokenThroughEurekaFeign() {
        FeignException exception = securityApi.expectStatus(401, securityApi.contextClient()::contextWithoutToken);

        assertEquals(401, exception.status());
    }

    @Test
    void shouldRejectProtectedContextWithMalformedTokenThroughEurekaFeign() {
        FeignException exception = securityApi.expectStatus(
                401,
                () -> securityApi.contextClient().context("Bearer invalid-token")
        );

        assertEquals(401, exception.status());
    }

    private void assertTokenResponse(TokenResponse response) {
        assertNotNull(response);
        assertNotNull(response.accessToken());
        assertFalse(response.accessToken().isBlank());
        assertNotNull(response.refreshToken());
        assertFalse(response.refreshToken().isBlank());
    }
}
