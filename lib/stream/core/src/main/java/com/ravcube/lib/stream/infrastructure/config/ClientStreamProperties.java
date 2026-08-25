package com.ravcube.lib.stream.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;

@Component
@ConfigurationProperties(prefix = "ravcube.stream")
public final class ClientStreamProperties {

    public static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(30);
    public static final int DEFAULT_MAX_IDS_PER_SUBSCRIPTION = 100;
    public static final int DEFAULT_MAX_SUBSCRIPTIONS = 1_000;
    public static final int DEFAULT_MAX_SUBSCRIPTIONS_PER_CLIENT = 100;
    public static final int DEFAULT_MAX_PENDING_EVENTS_PER_SUBSCRIPTION = 100;
    public static final Duration DEFAULT_HEARTBEAT_INTERVAL = Duration.ofSeconds(15);

    private Duration timeout = DEFAULT_TIMEOUT;
    private int maxIdsPerSubscription = DEFAULT_MAX_IDS_PER_SUBSCRIPTION;
    private int maxSubscriptions = DEFAULT_MAX_SUBSCRIPTIONS;
    private int maxSubscriptionsPerClient = DEFAULT_MAX_SUBSCRIPTIONS_PER_CLIENT;
    private int maxPendingEventsPerSubscription = DEFAULT_MAX_PENDING_EVENTS_PER_SUBSCRIPTION;
    private Duration heartbeatInterval = DEFAULT_HEARTBEAT_INTERVAL;

    public ClientStreamProperties() {
    }

    public ClientStreamProperties(Duration timeout) {
        this(
                timeout,
                DEFAULT_MAX_IDS_PER_SUBSCRIPTION,
                DEFAULT_MAX_SUBSCRIPTIONS
        );
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

    public ClientStreamProperties(
            Duration timeout,
            int maxIdsPerSubscription,
            int maxSubscriptions,
            int maxPendingEventsPerSubscription
    ) {
        this(
                timeout,
                maxIdsPerSubscription,
                maxSubscriptions,
                DEFAULT_MAX_SUBSCRIPTIONS_PER_CLIENT,
                maxPendingEventsPerSubscription,
                DEFAULT_HEARTBEAT_INTERVAL
        );
    }

    public ClientStreamProperties(
            Duration timeout,
            int maxIdsPerSubscription,
            int maxSubscriptions,
            int maxSubscriptionsPerClient,
            int maxPendingEventsPerSubscription,
            Duration heartbeatInterval
    ) {
        this.timeout = validateTimeout(timeout);
        this.maxIdsPerSubscription = validatePositive(
                maxIdsPerSubscription,
                "maxIdsPerSubscription"
        );
        this.maxSubscriptions = validatePositive(maxSubscriptions, "maxSubscriptions");
        this.maxSubscriptionsPerClient = validatePositive(
                maxSubscriptionsPerClient,
                "maxSubscriptionsPerClient"
        );
        this.maxPendingEventsPerSubscription = validatePositive(
                maxPendingEventsPerSubscription,
                "maxPendingEventsPerSubscription"
        );
        this.heartbeatInterval = validateTimeout(heartbeatInterval);
    }

    public Duration timeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = validateTimeout(timeout);
    }

    public int maxIdsPerSubscription() {
        return maxIdsPerSubscription;
    }

    public void setMaxIdsPerSubscription(int maxIdsPerSubscription) {
        this.maxIdsPerSubscription = validatePositive(
                maxIdsPerSubscription,
                "maxIdsPerSubscription"
        );
    }

    public int maxSubscriptions() {
        return maxSubscriptions;
    }

    public void setMaxSubscriptions(int maxSubscriptions) {
        this.maxSubscriptions = validatePositive(maxSubscriptions, "maxSubscriptions");
    }

    public int maxSubscriptionsPerClient() {
        return maxSubscriptionsPerClient;
    }

    public void setMaxSubscriptionsPerClient(int maxSubscriptionsPerClient) {
        this.maxSubscriptionsPerClient = validatePositive(
                maxSubscriptionsPerClient,
                "maxSubscriptionsPerClient"
        );
    }

    public int maxPendingEventsPerSubscription() {
        return maxPendingEventsPerSubscription;
    }

    public void setMaxPendingEventsPerSubscription(int maxPendingEventsPerSubscription) {
        this.maxPendingEventsPerSubscription = validatePositive(
                maxPendingEventsPerSubscription,
                "maxPendingEventsPerSubscription"
        );
    }

    public Duration heartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(Duration heartbeatInterval) {
        this.heartbeatInterval = validateTimeout(heartbeatInterval);
    }

    private static Duration validateTimeout(Duration value) {
        Objects.requireNonNull(value, "duration must not be null");
        if (value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException("duration must be greater than zero");
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
