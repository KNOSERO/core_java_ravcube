package com.ravcube.test.common.time;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestDelaysTest {

    @Test
    void shouldRunDelayedAction() {
        AtomicBoolean called = new AtomicBoolean(false);

        TestDelays.delayedBy(Duration.ZERO, () -> called.set(true)).run();

        assertTrue(called.get());
    }

    @Test
    void shouldRejectNegativeDelay() {
        assertThrows(
                IllegalArgumentException.class,
                () -> TestDelays.pause(Duration.ofMillis(-1))
        );
    }
}
