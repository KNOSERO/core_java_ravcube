package com.ravcube.lib.idempotency.web;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "${spring.application.name}",
        path = "/shots"
)
interface IdempotencyShotClient {

    @PostMapping
    ResponseEntity<String> shot(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody Map<String, String> payload
    );

    @PostMapping
    ResponseEntity<String> shotWithoutIdempotencyKey(@RequestBody Map<String, String> payload);
}
