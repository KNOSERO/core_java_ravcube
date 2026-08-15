package com.ravcube.lib.security.auth.service;

import com.ravcube.lib.security.auth.api.LoginRequest;
import com.ravcube.lib.security.auth.api.RefreshTokenRequest;
import com.ravcube.lib.security.auth.api.TokenResponse;
import com.ravcube.lib.security.auth.keycloak.KeycloakAuthClient;
import com.ravcube.lib.security.auth.keycloak.KeycloakAuthClient.KeycloakTokenResponse;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;

@Service
public class KeycloakAuthService {

    private static final String DEFAULT_CLIENT_ID = "admin-cli";

    private final KeycloakAuthClient keycloakAuthClient;
    private final String clientId;

    public KeycloakAuthService(
            KeycloakAuthClient keycloakAuthClient,
            @Value("${ravcube.keycloak.client-id:" + DEFAULT_CLIENT_ID + "}") String clientId
    ) {
        this.keycloakAuthClient = keycloakAuthClient;
        this.clientId = clientId;
    }

    public TokenResponse login(LoginRequest request) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", clientId);
        formData.add("username", request.username());
        formData.add("password", request.password());

        KeycloakTokenResponse response = requestToken(formData);
        requireToken(response.accessToken(), "access");
        requireToken(response.refreshToken(), "refresh");
        return new TokenResponse(response.accessToken(), response.refreshToken());
    }

    public TokenResponse refresh(RefreshTokenRequest request) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("client_id", clientId);
        formData.add("refresh_token", request.refreshToken());

        KeycloakTokenResponse response = requestToken(formData);
        requireToken(response.accessToken(), "access");
        requireToken(response.refreshToken(), "refresh");
        return new TokenResponse(response.accessToken(), response.refreshToken());
    }

    public void logout(RefreshTokenRequest request) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("refresh_token", request.refreshToken());

        exchangeWithKeycloak(() -> {
            keycloakAuthClient.logout(formData);
            return null;
        });
    }

    private KeycloakTokenResponse requestToken(MultiValueMap<String, String> formData) {
        KeycloakTokenResponse response = exchangeWithKeycloak(() -> keycloakAuthClient.token(formData));
        if (response == null) {
            throw new ResponseStatusException(BAD_GATEWAY, "Missing token response body from Keycloak");
        }
        return response;
    }

    private <T> T exchangeWithKeycloak(KeycloakCall<T> call) {
        try {
            return call.execute();
        } catch (FeignException exception) {
            throw new ResponseStatusException(
                    keycloakStatus(exception),
                    exception.contentUTF8(),
                    exception
            );
        }
    }

    private HttpStatus keycloakStatus(FeignException exception) {
        if (exception.status() < 100) {
            return BAD_GATEWAY;
        }
        return HttpStatus.valueOf(exception.status());
    }

    private void requireToken(String token, String tokenName) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(BAD_GATEWAY, "Missing " + tokenName + " token from Keycloak response");
        }
    }

    @FunctionalInterface
    private interface KeycloakCall<T> {

        T execute();
    }
}
