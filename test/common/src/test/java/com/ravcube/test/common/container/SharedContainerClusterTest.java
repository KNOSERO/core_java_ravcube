package com.ravcube.test.common.container;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.testcontainers.lifecycle.Startable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SharedContainerClusterTest {

    @Test
    void rejectsRequestedCountLowerThanOne() {
        SharedContainerCluster<FakeStartable> cluster = new SharedContainerCluster<>();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cluster.start("redis:7", 0, FakeStartable::new, FakeStartable::isRunning, index -> "stop-" + index)
        );

        assertEquals("requestedCount must be >= 1 but was 0", exception.getMessage());
    }

    @Test
    void createsRequestedContainers() {
        SharedContainerCluster<FakeStartable> cluster = new SharedContainerCluster<>();

        List<FakeStartable> containers = cluster.start(
                "redis:7",
                2,
                FakeStartable::new,
                FakeStartable::isRunning,
                index -> "stop-" + index
        );

        assertEquals(2, containers.size());
        assertTrue(containers.stream().allMatch(FakeStartable::isRunning));
        assertEquals(1, containers.get(0).starts());
        assertEquals(1, containers.get(1).starts());
    }

    @Test
    void reusesExistingContainersWhenCountDoesNotIncrease() {
        SharedContainerCluster<FakeStartable> cluster = new SharedContainerCluster<>();
        List<FakeStartable> firstStart = cluster.start(
                "redis:7",
                1,
                FakeStartable::new,
                FakeStartable::isRunning,
                index -> "stop-" + index
        );

        List<FakeStartable> secondStart = cluster.start(
                "redis:7",
                1,
                FakeStartable::new,
                FakeStartable::isRunning,
                index -> "stop-" + index
        );

        assertSame(firstStart.getFirst(), secondStart.getFirst());
        assertEquals(1, firstStart.getFirst().starts());
    }

    @Test
    void provisionsOnlyMissingContainersWhenCountIncreases() {
        SharedContainerCluster<FakeStartable> cluster = new SharedContainerCluster<>();
        List<FakeStartable> firstStart = cluster.start(
                "redis:7",
                1,
                FakeStartable::new,
                FakeStartable::isRunning,
                index -> "stop-" + index
        );

        List<FakeStartable> secondStart = cluster.start(
                "redis:7",
                3,
                FakeStartable::new,
                FakeStartable::isRunning,
                index -> "stop-" + index
        );

        assertSame(firstStart.getFirst(), secondStart.getFirst());
        assertEquals(3, secondStart.size());
        assertTrue(secondStart.stream().allMatch(FakeStartable::isRunning));
    }

    @Test
    void restartsStoppedSelectedContainer() {
        SharedContainerCluster<FakeStartable> cluster = new SharedContainerCluster<>();
        FakeStartable container = cluster.start(
                "redis:7",
                1,
                FakeStartable::new,
                FakeStartable::isRunning,
                index -> "stop-" + index
        ).getFirst();
        container.stop();

        cluster.start("redis:7", 1, FakeStartable::new, FakeStartable::isRunning, index -> "stop-" + index);

        assertTrue(container.isRunning());
        assertEquals(2, container.starts());
    }

    @Test
    void rejectsImageChangeWithinOneJvm() {
        SharedContainerCluster<FakeStartable> cluster = new SharedContainerCluster<>();
        cluster.start("redis:7", 1, FakeStartable::new, FakeStartable::isRunning, index -> "stop-" + index);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> cluster.start("redis:8", 1, FakeStartable::new, FakeStartable::isRunning, index -> "stop-" + index)
        );

        assertEquals(
                "Container image cannot change within one JVM. Existing: redis:7, requested: redis:8",
                exception.getMessage()
        );
    }

    @Test
    void returnsImmutableSnapshot() {
        SharedContainerCluster<FakeStartable> cluster = new SharedContainerCluster<>();
        List<FakeStartable> containers = cluster.start(
                "redis:7",
                1,
                FakeStartable::new,
                FakeStartable::isRunning,
                index -> "stop-" + index
        );

        assertThrows(UnsupportedOperationException.class, () -> containers.add(new FakeStartable("redis:7")));
    }

    private static final class FakeStartable implements Startable {

        private final String imageName;
        private final AtomicInteger starts = new AtomicInteger();
        private boolean running;

        private FakeStartable(String imageName) {
            this.imageName = imageName;
        }

        @Override
        public void start() {
            starts.incrementAndGet();
            running = true;
        }

        @Override
        public void stop() {
            running = false;
        }

        private boolean isRunning() {
            return running;
        }

        private int starts() {
            return starts.get();
        }
    }
}
