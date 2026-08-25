package com.ravcube.lib.stream.infrastructure.sse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class ClientStreamSubscriptionIndex {

    private final Set<ClientStreamRegisteredSubscription> subscriptions = new HashSet<>();
    private final Map<SubscriptionKey, Set<ClientStreamRegisteredSubscription>> subscriptionsByKey =
            new HashMap<>();

    void add(ClientStreamRegisteredSubscription subscription) {
        subscriptions.add(subscription);
        for (String resourceId : subscription.resourceIds()) {
            subscriptionsByKey
                    .computeIfAbsent(
                            new SubscriptionKey(subscription.resourceName(), resourceId),
                            ignored -> new HashSet<>()
                    )
                    .add(subscription);
        }
    }

    Set<ClientStreamRegisteredSubscription> matching(String resourceName, String resourceId) {
        return Set.copyOf(
                subscriptionsByKey.getOrDefault(
                        new SubscriptionKey(resourceName, resourceId),
                        Set.of()
                )
        );
    }

    ClientStreamRegisteredSubscription removeByEmitter(Object emitter) {
        for (ClientStreamRegisteredSubscription subscription : subscriptions) {
            if (subscription.emitter() == emitter) {
                remove(subscription);
                return subscription;
            }
        }
        return null;
    }

    void remove(ClientStreamRegisteredSubscription subscription) {
        if (!subscriptions.remove(subscription)) {
            return;
        }

        for (String resourceId : subscription.resourceIds()) {
            final SubscriptionKey key = new SubscriptionKey(
                    subscription.resourceName(),
                    resourceId
            );
            final Set<ClientStreamRegisteredSubscription> matching =
                    subscriptionsByKey.get(key);
            if (matching == null) {
                continue;
            }
            matching.remove(subscription);
            if (matching.isEmpty()) {
                subscriptionsByKey.remove(key);
            }
        }
    }

    boolean contains(ClientStreamRegisteredSubscription subscription) {
        return subscriptions.contains(subscription);
    }

    Set<ClientStreamRegisteredSubscription> snapshot() {
        return Set.copyOf(subscriptions);
    }

    int size() {
        return subscriptions.size();
    }

    private record SubscriptionKey(String resourceName, String resourceId) {
    }
}
