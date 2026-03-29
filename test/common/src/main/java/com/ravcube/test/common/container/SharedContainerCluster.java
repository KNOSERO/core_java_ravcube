package com.ravcube.test.common.container;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import org.testcontainers.lifecycle.Startable;

public final class SharedContainerCluster<C extends Startable> {

    private final List<C> containers = new ArrayList<>();
    private String imageName;

    public synchronized List<C> start(
            String requestedImageName,
            int requestedCount,
            Function<String, C> containerFactory,
            Predicate<C> runningChecker,
            IntFunction<String> shutdownHookNameFactory
    ) {
        if (requestedCount < 1) {
            throw new IllegalArgumentException("requestedCount must be >= 1 but was " + requestedCount);
        }

        if (imageName == null) {
            imageName = requestedImageName;
        } else if (!Objects.equals(imageName, requestedImageName)) {
            throw new IllegalStateException(
                    "Container image cannot change within one JVM. Existing: %s, requested: %s"
                            .formatted(imageName, requestedImageName)
            );
        }

        while (containers.size() < requestedCount) {
            int containerIndex = containers.size() + 1;
            C container = containerFactory.apply(requestedImageName);
            container.start();
            Runtime.getRuntime().addShutdownHook(new Thread(
                    container::stop,
                    shutdownHookNameFactory.apply(containerIndex)
            ));
            containers.add(container);
        }

        List<C> selectedContainers = containers.subList(0, requestedCount);
        selectedContainers.stream()
                .filter(container -> !runningChecker.test(container))
                .forEach(Startable::start);
        return List.copyOf(selectedContainers);
    }
}
