package com.ravcube.lib.stream.infrastructure.sse;

import com.ravcube.lib.stream.domain.ClientStreamSubscription;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

final class ClientStreamRegisteredSubscription {

    private final ClientStreamSubscription subscription;
    private final SseEmitter emitter;
    private final String clientKey;
    private final int maxPendingEvents;
    private final Deque<ClientStreamRefreshNotification> pending = new ArrayDeque<>();
    private final Map<String, Long> latestVersions = new HashMap<>();

    private boolean sending;
    private volatile ScheduledFuture<?> heartbeat;

    ClientStreamRegisteredSubscription(
            ClientStreamSubscription subscription,
            SseEmitter emitter,
            String clientKey,
            int maxPendingEvents
    ) {
        this.subscription = subscription;
        this.emitter = emitter;
        this.clientKey = clientKey;
        this.maxPendingEvents = maxPendingEvents;
    }

    String resourceName() {
        return subscription.getResourceName();
    }

    Set<String> resourceIds() {
        return subscription.getResourceIds();
    }

    String clientKey() {
        return clientKey;
    }

    SseEmitter emitter() {
        return emitter;
    }

    boolean accepts(String resourceName, String resourceId) {
        return subscription.accepts(resourceName, resourceId);
    }

    synchronized ClientStreamEnqueueResult enqueue(
            ClientStreamRefreshNotification notification
    ) {
        final long latestVersion = latestVersions.getOrDefault(
                notification.resourceId(),
                -1L
        );
        if (notification.version() <= latestVersion) {
            return ClientStreamEnqueueResult.DUPLICATE;
        }
        if (pending.size() >= maxPendingEvents) {
            return ClientStreamEnqueueResult.OVERFLOW;
        }

        latestVersions.put(notification.resourceId(), notification.version());
        pending.addLast(notification);
        if (!sending) {
            sending = true;
            return ClientStreamEnqueueResult.SCHEDULE;
        }
        return ClientStreamEnqueueResult.QUEUED;
    }

    synchronized ClientStreamRefreshNotification next() {
        if (pending.isEmpty()) {
            sending = false;
            return null;
        }
        return pending.removeFirst();
    }

    void heartbeat(ScheduledFuture<?> heartbeat) {
        this.heartbeat = heartbeat;
    }

    void cancelHeartbeat() {
        final ScheduledFuture<?> task = heartbeat;
        if (task != null) {
            task.cancel(false);
        }
    }
}

enum ClientStreamEnqueueResult {
    SCHEDULE,
    QUEUED,
    DUPLICATE,
    OVERFLOW
}
