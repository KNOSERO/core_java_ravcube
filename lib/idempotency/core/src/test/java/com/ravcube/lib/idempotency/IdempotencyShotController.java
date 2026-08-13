package com.ravcube.lib.idempotency;

import io.github.josipmusa.idempotency.spring.web.Idempotent;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
class IdempotencyShotController {

    private final AtomicInteger invocations = new AtomicInteger();

    @Idempotent(ttl = "PT1M", lockTimeout = "PT0.2S")
    @PostMapping("/shots")
    Map<String, Object> shot(@RequestHeader(value = "X-Test-Slow", defaultValue = "false") boolean slow) {
        if (slow) {
            sleep(500);
        }
        return Map.of("invocations", invocations.incrementAndGet());
    }

    void reset() {
        invocations.set(0);
    }

    int invocations() {
        return invocations.get();
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while handling test shot", interruptedException);
        }
    }
}
