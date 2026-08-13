package com.ravcube.lib.idempotency;

import com.ravcube.lib.eureka.RavcubeEurekaClient;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@RavcubeEurekaClient(
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
    ResponseEntity<String> slowShot(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestHeader("X-Test-Slow") boolean slow,
            @RequestBody Map<String, String> payload
    );

    @PostMapping
    ResponseEntity<String> shotWithoutIdempotencyKey(@RequestBody Map<String, String> payload);
}
