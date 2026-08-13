package com.ravcube.lib.idempotency;

import com.ravcube.test.common.time.TestDelays;
import io.github.josipmusa.idempotency.spring.web.Idempotent;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
class IdempotencyShotController {

    private static final Duration SLOW_SHOT_DELAY = Duration.ofMillis(500);

    private final AtomicInteger invocations = new AtomicInteger();

    @Idempotent(ttl = "PT1M", lockTimeout = "PT0.2S")
    @PostMapping("/shots")
    Map<String, Object> shot(@RequestHeader(value = "X-Test-Slow", defaultValue = "false") boolean slow) {
        if (slow) {
            TestDelays.pause(SLOW_SHOT_DELAY);
        }
        return Map.of("invocations", invocations.incrementAndGet());
    }

    void reset() {
        invocations.set(0);
    }

    int invocations() {
        return invocations.get();
    }
}
