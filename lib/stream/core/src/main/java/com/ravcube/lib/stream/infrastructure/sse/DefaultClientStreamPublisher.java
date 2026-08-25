package com.ravcube.lib.stream.infrastructure.sse;

import com.ravcube.lib.stream.api.ClientStreamPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Objects;

@Component
public final class DefaultClientStreamPublisher implements ClientStreamPublisher {

    private final ClientStreamRegistry registry;

    public DefaultClientStreamPublisher(ClientStreamRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
    }

    @Override
    public void publish(String resourceName, String resourceId, Object payload) {
        final String validatedResourceName = requireText(resourceName, "resourceName");
        final String validatedResourceId = requireText(resourceId, "resourceId");
        Objects.requireNonNull(payload, "payload must not be null");

        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    registry.publish(validatedResourceName, validatedResourceId, payload);
                }
            });
            return;
        }

        registry.publish(validatedResourceName, validatedResourceId, payload);
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
