package com.ravcube.lib.security.auth.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "ravcubeKeycloakAuthClient",
        url = "${spring.security.oauth2.resourceserver.jwt.issuer-uri}",
        path = "/protocol/openid-connect"
)
public interface KeycloakAuthClient {

    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    KeycloakTokenResponse token(@RequestBody MultiValueMap<String, String> formData);

    @PostMapping(value = "/logout", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    void logout(@RequestBody MultiValueMap<String, String> formData);

    record KeycloakTokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("refresh_token") String refreshToken
    ) {
    }
}
