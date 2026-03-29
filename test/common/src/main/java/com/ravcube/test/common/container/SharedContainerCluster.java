package com.ravcube.test.common.container;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import org.testcontainers.lifecycle.Startable;

public final class SharedContainerCluster<C extends Startable> {

    private final List<C> sharedContainers = new ArrayList<>();
    private String pinnedImageName;

    public synchronized List<C> start(
            String requestedImageName,
            int requestedCount,
            Function<String, C> containerFactory,
            Predicate<C> runningChecker,
            IntFunction<String> shutdownHookNameFactory
    ) {
        validateRequestedCount(requestedCount);
        pinImageOrValidate(requestedImageName);
        provisionMissingContainers(requestedImageName, requestedCount, containerFactory, shutdownHookNameFactory);
        List<C> selectedContainers = selectFirst(requestedCount);
        ensureSelectedContainersRunning(selectedContainers, runningChecker);
        return List.copyOf(selectedContainers);
    }

    private void validateRequestedCount(int requestedCount) {
        if (requestedCount < 1) {
            throw new IllegalArgumentException("requestedCount must be >= 1 but was " + requestedCount);
        }
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

    private void provisionMissingContainers(
            String requestedImageName,
            int requestedCount,
            Function<String, C> containerFactory,
            IntFunction<String> shutdownHookNameFactory
    ) {
        while (sharedContainers.size() < requestedCount) {
            int containerIndex = sharedContainers.size() + 1;
            C container = containerFactory.apply(requestedImageName);
            container.start();
            Runtime.getRuntime().addShutdownHook(new Thread(
                    container::stop,
                    shutdownHookNameFactory.apply(containerIndex)
            ));
            sharedContainers.add(container);
        }
    }

    private List<C> selectFirst(int requestedCount) {
        return sharedContainers.subList(0, requestedCount);
    }

    private void ensureSelectedContainersRunning(List<C> selectedContainers, Predicate<C> runningChecker) {
        selectedContainers.stream()
                .filter(container -> !runningChecker.test(container))
                .forEach(Startable::start);
    }
}
