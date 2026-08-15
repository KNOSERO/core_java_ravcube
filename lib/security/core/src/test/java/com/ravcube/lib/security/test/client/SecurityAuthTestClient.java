package com.ravcube.lib.security.test.client;

import com.ravcube.lib.security.auth.api.LoginRequest;
import com.ravcube.lib.security.auth.api.RefreshTokenRequest;
import com.ravcube.lib.security.auth.api.TokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "${spring.application.name}",
        contextId = "securityAuthTestClient",
        path = "${ravcube.security.auth.path:/auth}"
)
public interface SecurityAuthTestClient {

    @PostMapping("/login")
    TokenResponse login(@RequestBody LoginRequest request);

    @PostMapping("/refresh")
    TokenResponse refresh(@RequestBody RefreshTokenRequest request);

    @PostMapping("/logout")
    void logout(@RequestBody RefreshTokenRequest request);
}
