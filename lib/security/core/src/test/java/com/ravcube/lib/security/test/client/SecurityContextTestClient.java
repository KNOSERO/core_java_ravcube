package com.ravcube.lib.security.test.client;

import com.ravcube.lib.security.test.support.SecurityContextResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "${spring.application.name}",
        contextId = "securityContextTestClient",
        path = "/test/security"
)
public interface SecurityContextTestClient {

    @GetMapping("/context")
    SecurityContextResponse context(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization);

    @GetMapping("/context")
    String contextWithoutToken();
}
