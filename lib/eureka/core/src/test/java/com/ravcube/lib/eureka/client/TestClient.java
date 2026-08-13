package com.ravcube.lib.eureka.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "${spring.application.name}",
        path = "/test/eureka"
)
public interface TestClient {

    @GetMapping("/ping")
    String ping();
}
