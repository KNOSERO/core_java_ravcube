package com.ravcube.test.common.container;

import java.util.function.Function;
import java.util.function.Predicate;
import org.testcontainers.lifecycle.Startable;

public final class SharedContainer<C extends Startable> {

    private final SharedContainerCluster<C> cluster = new SharedContainerCluster<>();

    public synchronized C start(
            String requestedImageName,
            Function<String, C> containerFactory,
            Predicate<C> runningChecker,
            String shutdownHookName
    ) {
        return cluster.start(
                requestedImageName,
                1,
                containerFactory,
                runningChecker,
                ignored -> shutdownHookName
        ).getFirst();
    }
}
