package com.ravcube.lib.faulttolerance.support;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        contextId = "unavailableServiceClient",
        name = "unavailable-service",
        path = "/downstream",
        fallback = UnavailableServiceFallback.class
)
public interface UnavailableServiceClient {

    @GetMapping("/status")
    String status();
}
