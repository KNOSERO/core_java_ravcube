package com.ravcube.lib.security.test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ravcube.lib.security.SecurityContext;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;

@RestController
@RequestMapping("/test/security")
class SecurityTestController {

    private static final String DEFAULT_CLIENT_ID = "admin-cli";
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @PostMapping("/login")
    LoginResponse login(@RequestBody LoginRequest request) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", DEFAULT_CLIENT_ID);
        formData.add("username", request.username());
        formData.add("password", request.password());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<KeycloakTokenResponse> response = restTemplate.exchange(
                issuerUri + "/protocol/openid-connect/token",
                HttpMethod.POST,
                new HttpEntity<>(formData, headers),
                KeycloakTokenResponse.class);

        KeycloakTokenResponse body = response.getBody();
        if (body == null || body.accessToken() == null || body.accessToken().isBlank()) {
            throw new ResponseStatusException(BAD_GATEWAY, "Missing access token from Keycloak response");
        }

        return new LoginResponse(body.accessToken());
    }

    @GetMapping("/context")
    SecurityContextResponse context() {
        return new SecurityContextResponse(SecurityContext.getRoles(), SecurityContext.getClaims());
    }

    record LoginRequest(String username, String password) {
    }

    record LoginResponse(String accessToken) {
    }

    record SecurityContextResponse(List<String> roles, Map<String, Object> claims) {
    }

    private record KeycloakTokenResponse(@JsonProperty("access_token") String accessToken) {
    }
}
