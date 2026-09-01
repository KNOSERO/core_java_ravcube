package com.ravcube.lib.security.test.support;

import com.ravcube.lib.security.auth.api.LoginRequest;
import com.ravcube.lib.security.auth.api.RefreshTokenRequest;
import com.ravcube.lib.security.auth.api.TokenResponse;
import com.ravcube.lib.security.test.client.SecurityAuthTestClient;
import com.ravcube.lib.security.test.client.SecurityContextTestClient;
import feign.FeignException;
import java.time.Duration;
import org.springframework.stereotype.Component;

import static com.ravcube.test.awaitility.Eventually.untilSucceeds;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Component
public class SecurityApiScenarioClient {

    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration RETRY_DELAY = Duration.ofMillis(250);

    private final SecurityAuthTestClient authClient;
    private final SecurityContextTestClient contextClient;

    public SecurityApiScenarioClient(SecurityAuthTestClient authClient, SecurityContextTestClient contextClient) {
        this.authClient = authClient;
        this.contextClient = contextClient;
    }

    public TokenResponse login(String username, String password) {
        return untilSucceeds(WAIT_TIMEOUT, RETRY_DELAY, () -> authClient.login(new LoginRequest(username, password)));
    }

    public TokenResponse refresh(String refreshToken) {
        return untilSucceeds(
                WAIT_TIMEOUT,
                RETRY_DELAY,
                () -> authClient.refresh(new RefreshTokenRequest(refreshToken))
        );
    }

    public void logout(String refreshToken) {
        untilSucceeds(WAIT_TIMEOUT, RETRY_DELAY, () -> {
            authClient.logout(new RefreshTokenRequest(refreshToken));
            return true;
        });
    }

    public SecurityContextResponse context(String accessToken) {
        return untilSucceeds(WAIT_TIMEOUT, RETRY_DELAY, () -> contextClient.context("Bearer " + accessToken));
    }

    public FeignException expectStatus(int status, FeignCall call) {
        return untilSucceeds(WAIT_TIMEOUT, RETRY_DELAY, () -> {
            FeignException exception = expectFeignException(call);
            assertEquals(status, exception.status());
            return exception;
        });
    }

    public FeignException expectClientError(FeignCall call) {
        return untilSucceeds(WAIT_TIMEOUT, RETRY_DELAY, () -> {
            FeignException exception = expectFeignException(call);
            if (exception.status() < 400 || exception.status() >= 500) {
                throw new AssertionError("Expected a client error, got HTTP " + exception.status());
            }
            return exception;
        });
    }

    public FeignException expectFeignException(FeignCall call) {
        try {
            call.execute();
            throw new IllegalStateException("Expected Feign exception");
        } catch (FeignException exception) {
            return exception;
        }
    }

    public SecurityAuthTestClient authClient() {
        return authClient;
    }

    public SecurityContextTestClient contextClient() {
        return contextClient;
    }

    @FunctionalInterface
    public interface FeignCall {

        void execute();
    }
}
