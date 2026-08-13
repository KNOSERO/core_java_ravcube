package com.ravcube.test.common.time;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class TestDelays {

    private TestDelays() {
    }

    public static void pause(Duration duration) {
        Objects.requireNonNull(duration, "duration must not be null");
        if (duration.isNegative()) {
            throw new IllegalArgumentException("duration must not be negative");
        }

        try {
            TimeUnit.NANOSECONDS.sleep(duration.toNanos());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while applying test delay", exception);
        }
    }

    public static Runnable delayedBy(Duration delay, Runnable action) {
        Objects.requireNonNull(action, "action must not be null");
        return () -> {
            pause(delay);
            action.run();
        };
    }
}
