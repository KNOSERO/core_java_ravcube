package com.ravcube.test.awaitility;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventuallyTest {

    @Test
    void shouldReturnValueWhenOperationEventuallySucceeds() {
        AtomicInteger attempts = new AtomicInteger();

        String result = Eventually.untilSucceeds(
                Duration.ofSeconds(1),
                Duration.ofMillis(10),
                () -> {
                    if (attempts.incrementAndGet() < 3) {
                        throw new IllegalStateException("not yet");
                    }
                    return "done";
                }
        );

        assertEquals("done", result);
    }

    @Test
    void shouldReturnExpectedExceptionWhenOperationEventuallyThrowsIt() {
        IllegalArgumentException expected = new IllegalArgumentException("expected");
        AtomicInteger attempts = new AtomicInteger();

        IllegalArgumentException result = Eventually.untilThrows(
                Duration.ofSeconds(1),
                Duration.ofMillis(10),
                IllegalArgumentException.class,
                () -> {
                    if (attempts.incrementAndGet() < 3) {
                        throw new IllegalStateException("not yet");
                    }
                    throw expected;
                },
                exception -> exception.getMessage().equals("expected")
        );

        assertSame(expected, result);
    }

    @Test
    void shouldWaitUntilBooleanConditionIsTrue() {
        AtomicInteger attempts = new AtomicInteger();

        Eventually.until(
                Duration.ofSeconds(1),
                Duration.ofMillis(10),
                () -> attempts.incrementAndGet() >= 3
        );

        assertTrue(attempts.get() >= 3);
    }

    @Test
    void shouldWaitUntilAssertionPasses() {
        AtomicReference<String> value = new AtomicReference<>("pending");

        Eventually.untilAsserted(
                Duration.ofSeconds(1),
                Duration.ofMillis(10),
                () -> {
                    if (value.get().equals("pending")) {
                        value.set("done");
                    }
                    assertEquals("done", value.get());
                }
        );
    }

    @Test
    void shouldTimeoutWhenExpectedExceptionNeverHappens() {
        assertThrows(
                ConditionTimeoutException.class,
                () -> Eventually.untilThrows(
                        Duration.ofMillis(50),
                        Duration.ofMillis(10),
                        IllegalArgumentException.class,
                        () -> {
                        },
                        exception -> true
                )
        );
    }
}
