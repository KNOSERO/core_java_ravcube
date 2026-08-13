package com.ravcube.lib.eureka;

import feign.FeignException;
import java.util.OptionalInt;

public final class RavcubeEurekaClientExceptions {

    private RavcubeEurekaClientExceptions() {
    }

    public static OptionalInt status(Throwable exception) {
        if (exception instanceof FeignException feignException) {
            return OptionalInt.of(feignException.status());
        }
        return OptionalInt.empty();
    }

    public static boolean hasStatus(Throwable exception, int expectedStatus) {
        return status(exception)
                .stream()
                .anyMatch(status -> status == expectedStatus);
    }
}
