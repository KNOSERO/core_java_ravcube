package com.ravcube.lib.faulttolerance.support;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class UnavailableServiceFallback implements UnavailableServiceClient {

    private final AtomicInteger invocations = new AtomicInteger();

    @Override
    public String status() {
        invocations.incrementAndGet();
        return "fallback";
    }

    public int invocations() {
        return invocations.get();
    }

    public void reset() {
        invocations.set(0);
    }
}
