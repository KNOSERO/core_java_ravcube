package com.ravcube.lib.stream.infrastructure.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;

@Component
public final class ClientStreamProperties {

    public static final int DEFAULT_MAX_IDS_PER_SUBSCRIPTION = 100;
    public static final int DEFAULT_MAX_SUBSCRIPTIONS = 1_000;
    public static final int DEFAULT_MAX_PENDING_EVENTS_PER_SUBSCRIPTION = 100;

    private final Duration timeout;
    private final int maxIdsPerSubscription;
    private final int maxSubscriptions;
    private final int maxPendingEventsPerSubscription;

    @Autowired
    public ClientStreamProperties(
            @Value("${ravcube.stream.timeout:PT30M}") Duration timeout,
            @Value("${ravcube.stream.max-ids-per-subscription:100}") int maxIdsPerSubscription,
            @Value("${ravcube.stream.max-subscriptions:1000}") int maxSubscriptions,
            @Value("${ravcube.stream.max-pending-events-per-subscription:100}") int maxPendingEventsPerSubscription
    ) {
        this.timeout = validateTimeout(timeout);
        this.maxIdsPerSubscription = validatePositive(
                maxIdsPerSubscription,
                "maxIdsPerSubscription"
        );
        this.maxSubscriptions = validatePositive(maxSubscriptions, "maxSubscriptions");
        this.maxPendingEventsPerSubscription = validatePositive(
                maxPendingEventsPerSubscription,
                "maxPendingEventsPerSubscription"
        );
    }

    public ClientStreamProperties(Duration timeout) {
        this(timeout, DEFAULT_MAX_IDS_PER_SUBSCRIPTION, DEFAULT_MAX_SUBSCRIPTIONS);
    }

    public ClientStreamProperties(
            Duration timeout,
            int maxIdsPerSubscription,
            int maxSubscriptions
    ) {
        this(
                timeout,
                maxIdsPerSubscription,
                maxSubscriptions,
                DEFAULT_MAX_PENDING_EVENTS_PER_SUBSCRIPTION
        );
    }

    public Duration timeout() {
        return timeout;
    }

    public int maxIdsPerSubscription() {
        return maxIdsPerSubscription;
    }

    public int maxSubscriptions() {
        return maxSubscriptions;
    }

    public int maxPendingEventsPerSubscription() {
        return maxPendingEventsPerSubscription;
    }

    private static Duration validateTimeout(Duration value) {
        Objects.requireNonNull(value, "timeout must not be null");
        if (value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException("timeout must be greater than zero");
        }
        return value;
    }

    private static int validatePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be greater than zero");
        }
        return value;
    }
}
