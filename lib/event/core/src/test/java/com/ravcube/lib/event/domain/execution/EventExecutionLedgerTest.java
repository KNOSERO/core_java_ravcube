package com.ravcube.lib.event.domain.execution;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventExecutionLedgerTest {

    @Test
    void shouldTrackPerEventInvocations() {
        EventExecutionLedger<TestEvent, UUID> ledger = EventExecutionLedger.of(TestEvent::id);
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();

        ledger.register(new TestEvent(firstId, "first"));
        ledger.register(new TestEvent(firstId, "first-again"));
        ledger.register(new TestEvent(secondId, "second"));

        assertEquals(2, ledger.invocations(firstId));
        assertEquals(1, ledger.invocations(secondId));
        assertEquals(0, ledger.invocations(UUID.randomUUID()));
    }

    @Test
    void shouldResetInvocations() {
        EventExecutionLedger<TestEvent, UUID> ledger = EventExecutionLedger.of(TestEvent::id);
        UUID id = UUID.randomUUID();

        ledger.register(new TestEvent(id, "before-reset"));
        ledger.register(new TestEvent(id, "before-reset-again"));

        ledger.reset();

        assertEquals(0, ledger.invocations(id));
    }

    @Test
    void shouldHandleConcurrentEventRegistrations() throws InterruptedException {
        EventExecutionLedger<TestEvent, UUID> ledger = EventExecutionLedger.of(TestEvent::id);
        List<UUID> ids = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ids.add(UUID.randomUUID());
        }

        int eventsPerId = 50;
        ExecutorService executor = Executors.newFixedThreadPool(8);
        for (UUID id : ids) {
            for (int i = 0; i < eventsPerId; i++) {
                executor.submit(() -> ledger.register(new TestEvent(id, "parallel")));
            }
        }

        executor.shutdown();
        boolean finished = executor.awaitTermination(5, TimeUnit.SECONDS);

        assertTrue(finished);
        ids.forEach(id -> assertEquals(eventsPerId, ledger.invocations(id)));
    }

    @Test
    void shouldWaitForInvocationsUsingDefaultDelay() throws InterruptedException {
        EventExecutionLedger<TestEvent, UUID> ledger = EventExecutionLedger.of(TestEvent::id);
        UUID id = UUID.randomUUID();
        Thread registrar = new Thread(() -> {
            sleep(100L);
            ledger.register(new TestEvent(id, "delayed"));
        });

        registrar.start();
        assertEquals(1, ledger.invocations(id));
        registrar.join();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while preparing delayed registration", ex);
        }
    }

    private record TestEvent(UUID id, String message) {
    }
}
