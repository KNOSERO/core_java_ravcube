package com.ravcube.lib.idempotency.web;

import io.github.josipmusa.idempotency.spring.web.Idempotent;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class IdempotencyShotController {

    private final AtomicInteger invocations = new AtomicInteger();

    @Idempotent(ttl = "PT1M")
    @PostMapping("/shots")
    Map<String, Object> shot() {
        return Map.of("invocations", invocations.incrementAndGet());
    }

    void reset() {
        invocations.set(0);
    }

    int invocations() {
        return invocations.get();
    }
}
