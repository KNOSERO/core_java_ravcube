package com.ravcube.test.common.container;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import org.testcontainers.lifecycle.Startable;

public final class SharedContainer<C extends Startable> {

    private C sharedContainer;
    private String pinnedImageName;

    public synchronized C start(
            String requestedImageName,
            Function<String, C> containerFactory,
            Predicate<C> runningChecker,
            String shutdownHookName
    ) {
        pinImageOrValidate(requestedImageName);
        startContainerIfNeeded(requestedImageName, containerFactory, runningChecker, shutdownHookName);
        return sharedContainer;
    }

    private void pinImageOrValidate(String requestedImageName) {
        if (pinnedImageName == null) {
            pinnedImageName = requestedImageName;
            return;
        }

        if (!Objects.equals(pinnedImageName, requestedImageName)) {
            throw new IllegalStateException(
                    "Container image cannot change within one JVM. Existing: %s, requested: %s"
                            .formatted(pinnedImageName, requestedImageName)
            );
        }
    }

    private void startContainerIfNeeded(
            String requestedImageName,
            Function<String, C> containerFactory,
            Predicate<C> runningChecker,
            String shutdownHookName
    ) {
        if (sharedContainer == null) {
            createAndStartContainer(requestedImageName, containerFactory, shutdownHookName);
            return;
        }

        if (!runningChecker.test(sharedContainer)) {
            sharedContainer.start();
        }
    }

    private void createAndStartContainer(
            String requestedImageName,
            Function<String, C> containerFactory,
            String shutdownHookName
    ) {
        sharedContainer = containerFactory.apply(requestedImageName);
        sharedContainer.start();
        Runtime.getRuntime().addShutdownHook(new Thread(sharedContainer::stop, shutdownHookName));
    }
}
