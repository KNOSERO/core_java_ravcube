package com.ravcube.test.awaitility;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;

public final class Eventually {

    private Eventually() {
    }

    public static <T> T untilSucceeds(Duration timeout, Duration pollInterval, Callable<T> operation) {
        Objects.requireNonNull(timeout, "timeout must not be null");
        Objects.requireNonNull(pollInterval, "pollInterval must not be null");
        Objects.requireNonNull(operation, "operation must not be null");

        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> lastFailure = new AtomicReference<>();
        AtomicBoolean completed = new AtomicBoolean(false);

        try {
            Awaitility.await()
                    .atMost(timeout)
                    .pollInterval(pollInterval)
                    .until(() -> {
                        try {
                            result.set(operation.call());
                            completed.set(true);
                            return true;
                        } catch (Throwable exception) {
                            lastFailure.set(exception);
                            return false;
                        }
                    });
        } catch (ConditionTimeoutException timeoutException) {
            addLastFailure(timeoutException, lastFailure.get());
            throw timeoutException;
        }

        if (!completed.get()) {
            throw new IllegalStateException("Operation did not complete before timeout");
        }
        return result.get();
    }

    public static void until(Duration timeout, Duration pollInterval, BooleanSupplier condition) {
        Objects.requireNonNull(timeout, "timeout must not be null");
        Objects.requireNonNull(pollInterval, "pollInterval must not be null");
        Objects.requireNonNull(condition, "condition must not be null");

        Awaitility.await()
                .atMost(timeout)
                .pollInterval(pollInterval)
                .until(condition::getAsBoolean);
    }

    public static void untilAsserted(Duration timeout, Duration pollInterval, ThrowingRunnable assertion) {
        Objects.requireNonNull(timeout, "timeout must not be null");
        Objects.requireNonNull(pollInterval, "pollInterval must not be null");
        Objects.requireNonNull(assertion, "assertion must not be null");

        Awaitility.await()
                .atMost(timeout)
                .pollInterval(pollInterval)
                .untilAsserted(assertion::run);
    }

    public static <E extends Throwable> E untilThrows(
            Duration timeout,
            Duration pollInterval,
            Class<E> exceptionType,
            ThrowingRunnable operation,
            Predicate<E> expectedException
    ) {
        Objects.requireNonNull(timeout, "timeout must not be null");
        Objects.requireNonNull(pollInterval, "pollInterval must not be null");
        Objects.requireNonNull(exceptionType, "exceptionType must not be null");
        Objects.requireNonNull(operation, "operation must not be null");
        Objects.requireNonNull(expectedException, "expectedException must not be null");

        AtomicReference<E> result = new AtomicReference<>();
        AtomicReference<Throwable> lastFailure = new AtomicReference<>();

        try {
            Awaitility.await()
                    .atMost(timeout)
                    .pollInterval(pollInterval)
                    .until(() -> {
                        try {
                            operation.run();
                            return false;
                        } catch (Throwable exception) {
                            lastFailure.set(exception);
                            if (exceptionType.isInstance(exception)) {
                                E typedException = exceptionType.cast(exception);
                                if (expectedException.test(typedException)) {
                                    result.set(typedException);
                                    return true;
                                }
                            }
                            return false;
                        }
                    });
        } catch (ConditionTimeoutException timeoutException) {
            addLastFailure(timeoutException, lastFailure.get());
            throw timeoutException;
        }

        return result.get();
    }

    private static void addLastFailure(RuntimeException timeoutException, Throwable lastFailure) {
        if (lastFailure != null) {
            timeoutException.addSuppressed(lastFailure);
        }
    }

    @FunctionalInterface
    public interface ThrowingRunnable {

        void run() throws Exception;
    }
}
