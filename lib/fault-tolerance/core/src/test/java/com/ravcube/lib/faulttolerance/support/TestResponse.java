package com.ravcube.lib.faulttolerance.support;

import com.ravcube.test.common.time.TestDelays;
import java.time.Duration;

public record TestResponse(int status, String body, Duration delay) {

    public static TestResponse ok(String body) {
        return new TestResponse(200, body, Duration.ZERO);
    }

    public static TestResponse error(int status) {
        return new TestResponse(status, "error", Duration.ZERO);
    }

    public static TestResponse slow(Duration delay, String body) {
        return new TestResponse(200, body, delay);
    }

    void pause() {
        if (delay.isZero()) {
            return;
        }
        TestDelays.pause(delay);
    }
}
