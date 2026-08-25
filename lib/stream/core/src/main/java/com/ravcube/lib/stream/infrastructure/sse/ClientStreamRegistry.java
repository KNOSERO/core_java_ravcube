package com.ravcube.lib.stream.infrastructure.sse;

import com.ravcube.lib.logger.Logger;
import com.ravcube.lib.logger.LoggerFactory;
import com.ravcube.lib.stream.common.ClientStreamCapacityExceededException;
import com.ravcube.lib.stream.domain.ClientStreamSubscription;
import com.ravcube.lib.stream.infrastructure.config.ClientStreamProperties;
import com.ravcube.lib.stream.infrastructure.metrics.ClientStreamMetrics;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.function.LongFunction;

@Component
public final class ClientStreamRegistry implements DisposableBean {

    public static final String REFRESH_EVENT = "refresh";

    private static final String INTERNAL_CLIENT = "internal";
    private static final long RECONNECT_DELAY_MILLIS = 3_000L;

    private final Duration timeout;
    private final int maxIdsPerSubscription;
    private final int maxSubscriptions;
    private final int maxSubscriptionsPerClient;
    private final int maxPendingEventsPerSubscription;
    private final Duration heartbeatInterval;
    private final LongFunction<SseEmitter> emitterFactory;
    private final Executor sender;
    private final ExecutorService ownedSender;
    private final ScheduledExecutorService heartbeatScheduler;
    private final ScheduledExecutorService ownedHeartbeatScheduler;
    private final ClientStreamMetrics metrics;
    private final Logger logger;
    private final java.util.concurrent.CopyOnWriteArrayList<RegisteredSubscription> subscriptions =
            new java.util.concurrent.CopyOnWriteArrayList<>();
    private final Map<String, Integer> activeSubscriptionsByClient = new HashMap<>();

    @Autowired
    public ClientStreamRegistry(
            ClientStreamProperties properties,
            LoggerFactory loggerFactory,
            ClientStreamMetrics metrics
    ) {
        this(
                properties,
                timeout -> new SseEmitter(timeout),
                createSender(),
                true,
                loggerFactory.getLogger(ClientStreamRegistry.class),
                createHeartbeatScheduler(),
                true,
                metrics
        );
    }

    ClientStreamRegistry(
            ClientStreamProperties properties,
            LongFunction<SseEmitter> emitterFactory
    ) {
        this(
                properties,
                emitterFactory,
                Runnable::run,
                false,
                Logger.noop(),
                null,
                false,
                ClientStreamMetrics.noop()
        );
    }

    ClientStreamRegistry(
            ClientStreamProperties properties,
            LongFunction<SseEmitter> emitterFactory,
            Executor sender
    ) {
        this(
                properties,
                emitterFactory,
                sender,
                false,
                Logger.noop(),
                null,
                false,
                ClientStreamMetrics.noop()
        );
    }

    private ClientStreamRegistry(
            ClientStreamProperties properties,
            LongFunction<SseEmitter> emitterFactory,
            Executor sender,
            boolean ownsSender,
            Logger logger,
            ScheduledExecutorService heartbeatScheduler,
            boolean ownsHeartbeatScheduler,
            ClientStreamMetrics metrics
    ) {
        final ClientStreamProperties validatedProperties = Objects.requireNonNull(
                properties,
                "properties must not be null"
        );
        this.timeout = validatedProperties.timeout();
        this.maxIdsPerSubscription = validatedProperties.maxIdsPerSubscription();
        this.maxSubscriptions = validatedProperties.maxSubscriptions();
        this.maxSubscriptionsPerClient = validatedProperties.maxSubscriptionsPerClient();
        this.maxPendingEventsPerSubscription =
                validatedProperties.maxPendingEventsPerSubscription();
        this.heartbeatInterval = validatedProperties.heartbeatInterval();
        this.emitterFactory = Objects.requireNonNull(
                emitterFactory,
                "emitterFactory must not be null"
        );
        this.sender = Objects.requireNonNull(sender, "sender must not be null");
        this.ownedSender = ownsSender ? (ExecutorService) sender : null;
        this.heartbeatScheduler = heartbeatScheduler;
        this.ownedHeartbeatScheduler = ownsHeartbeatScheduler ? heartbeatScheduler : null;
        this.metrics = Objects.requireNonNull(metrics, "metrics must not be null");
        this.logger = Objects.requireNonNull(logger, "logger must not be null");
    }

    public SseEmitter subscribe(String resourceName, Collection<String> resourceIds) {
        return subscribe(resourceName, resourceIds, INTERNAL_CLIENT);
    }

    public SseEmitter subscribe(
            String resourceName,
            Collection<String> resourceIds,
            String clientKey
    ) {
        final String validatedResourceName = requireText(resourceName, "resourceName");
        final Set<String> validatedIds = validateIds(resourceIds, maxIdsPerSubscription);
        final String validatedClientKey = requireText(clientKey, "clientKey");
        final ClientStreamSubscription subscription = new ClientStreamSubscription(
                validatedResourceName,
                validatedIds
        );

        final SseEmitter emitter;
        final RegisteredSubscription registered;
        synchronized (subscriptions) {
            if (subscriptions.size() >= maxSubscriptions) {
                metrics.subscriptionRejected();
                throw new ClientStreamCapacityExceededException(
                        "Maximum number of stream subscriptions has been reached"
                );
            }

            final int clientSubscriptions = activeSubscriptionsByClient.getOrDefault(
                    validatedClientKey,
                    0
            );
            if (clientSubscriptions >= maxSubscriptionsPerClient) {
                metrics.subscriptionRejected();
                throw new ClientStreamCapacityExceededException(
                        "Maximum number of stream subscriptions for this client has been reached"
                );
            }

            emitter = Objects.requireNonNull(
                    emitterFactory.apply(timeout.toMillis()),
                    "emitterFactory returned null"
            );
            registered = new RegisteredSubscription(
                    subscription,
                    emitter,
                    validatedClientKey,
                    maxPendingEventsPerSubscription
            );
            subscriptions.add(registered);
            activeSubscriptionsByClient.merge(validatedClientKey, 1, Integer::sum);
            metrics.subscriptionOpened();
        }

        emitter.onCompletion(() -> remove(emitter));
        emitter.onTimeout(() -> {
            remove(emitter);
            emitter.complete();
        });
        emitter.onError(error -> remove(emitter));
        scheduleHeartbeat(registered);

        return emitter;
    }

    public void publish(String resourceName, String resourceId, long version) {
        final String validatedResourceName = requireText(resourceName, "resourceName");
        final ClientStreamRefreshNotification notification =
                new ClientStreamRefreshNotification(resourceId, version);

        for (RegisteredSubscription subscription : subscriptions) {
            if (!subscription.accepts(validatedResourceName, notification.resourceId())) {
                continue;
            }

            final EnqueueResult result = subscription.enqueue(notification);
            if (result == EnqueueResult.SCHEDULE) {
                schedule(subscription);
            } else if (result == EnqueueResult.OVERFLOW) {
                removeSlowSubscription(subscription);
            }
        }
    }

    private void schedule(RegisteredSubscription subscription) {
        try {
            sender.execute(() -> drain(subscription));
        } catch (RejectedExecutionException exception) {
            metrics.sendFailure();
            logger.error(
                    "SSE sender rejected a stream subscription for resource {}",
                    exception,
                    subscription.subscription.resourceName
            );
            remove(subscription.emitter);
            subscription.emitter.completeWithError(exception);
        }
    }

    private void drain(RegisteredSubscription subscription) {
        while (true) {
            final ClientStreamRefreshNotification notification = subscription.next();
            if (notification == null) {
                return;
            }

            try {
                synchronized (subscription.emitter) {
                    subscription.emitter.send(
                            SseEmitter.event()
                                    .name(REFRESH_EVENT)
                                    .data(notification)
                    );
                }
            } catch (IOException | IllegalStateException exception) {
                metrics.sendFailure();
                logger.warn(
                        "SSE send failed for resource {}",
                        subscription.subscription.resourceName
                );
                remove(subscription.emitter);
                subscription.emitter.completeWithError(exception);
                return;
            }
        }
    }

    private void scheduleHeartbeat(RegisteredSubscription subscription) {
        if (heartbeatScheduler == null) {
            return;
        }

        try {
            subscription.heartbeat = heartbeatScheduler.scheduleAtFixedRate(
                    () -> scheduleHeartbeatSend(subscription),
                    heartbeatInterval.toMillis(),
                    heartbeatInterval.toMillis(),
                    java.util.concurrent.TimeUnit.MILLISECONDS
            );
        } catch (RejectedExecutionException exception) {
            metrics.heartbeatFailure();
            remove(subscription.emitter);
            subscription.emitter.completeWithError(exception);
        }
    }

    private void scheduleHeartbeatSend(RegisteredSubscription subscription) {
        try {
            sender.execute(() -> sendHeartbeat(subscription));
        } catch (RejectedExecutionException exception) {
            metrics.heartbeatFailure();
            remove(subscription.emitter);
            subscription.emitter.completeWithError(exception);
        }
    }

    private void sendHeartbeat(RegisteredSubscription subscription) {
        if (!subscriptions.contains(subscription)) {
            return;
        }

        try {
            synchronized (subscription.emitter) {
                subscription.emitter.send(
                        SseEmitter.event()
                                .reconnectTime(RECONNECT_DELAY_MILLIS)
                                .comment("heartbeat")
                );
            }
        } catch (IOException | IllegalStateException exception) {
            metrics.heartbeatFailure();
            logger.warn(
                    "SSE heartbeat failed for resource {}",
                    subscription.subscription.resourceName
            );
            remove(subscription.emitter);
            subscription.emitter.completeWithError(exception);
        }
    }

    private void removeSlowSubscription(RegisteredSubscription subscription) {
        metrics.queueOverflow();
        logger.warn(
                "SSE subscription removed after reaching the pending event limit for resource {}",
                subscription.subscription.resourceName
        );
        remove(subscription.emitter);
        subscription.emitter.completeWithError(
                new IOException("SSE subscriber is too slow")
        );
    }

    private static ExecutorService createSender() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    private static ScheduledExecutorService createHeartbeatScheduler() {
        return Executors.newSingleThreadScheduledExecutor(runnable -> {
            final Thread thread = new Thread(runnable, "ravcube-stream-heartbeat");
            thread.setDaemon(true);
            return thread;
        });
    }

    private void remove(SseEmitter emitter) {
        RegisteredSubscription removedSubscription = null;
        synchronized (subscriptions) {
            for (RegisteredSubscription subscription : subscriptions) {
                if (subscription.emitter == emitter && subscriptions.remove(subscription)) {
                    decrementClientSubscription(subscription.clientKey);
                    removedSubscription = subscription;
                    break;
                }
            }
        }

        if (removedSubscription != null) {
            removedSubscription.cancelHeartbeat();
            metrics.subscriptionClosed();
        }
    }

    private void decrementClientSubscription(String clientKey) {
        final int remaining = activeSubscriptionsByClient.getOrDefault(clientKey, 0) - 1;
        if (remaining <= 0) {
            activeSubscriptionsByClient.remove(clientKey);
        } else {
            activeSubscriptionsByClient.put(clientKey, remaining);
        }
    }

    public void unsubscribe(SseEmitter emitter) {
        remove(Objects.requireNonNull(emitter, "emitter must not be null"));
    }

    int activeSubscriptions() {
        return subscriptions.size();
    }

    @Override
    public void destroy() {
        if (ownedSender != null) {
            ownedSender.shutdownNow();
        }
        if (ownedHeartbeatScheduler != null) {
            ownedHeartbeatScheduler.shutdownNow();
        }
    }

    private static Set<String> validateIds(
            Collection<String> resourceIds,
            int maxIdsPerSubscription
    ) {
        Objects.requireNonNull(resourceIds, "resourceIds must not be null");

        if (resourceIds.size() > maxIdsPerSubscription) {
            throw new ClientStreamCapacityExceededException(
                    "Maximum number of ids per stream subscription is " + maxIdsPerSubscription
            );
        }

        if (resourceIds.isEmpty()) {
            throw new IllegalArgumentException("resourceIds must not be empty");
        }

        return Collections.unmodifiableSet(Set.copyOf(resourceIds));
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    private enum EnqueueResult {
        SCHEDULE,
        QUEUED,
        DUPLICATE,
        OVERFLOW
    }

    private static final class RegisteredSubscription {

        private final ClientStreamSubscription subscription;
        private final SseEmitter emitter;
        private final String clientKey;
        private final int maxPendingEvents;
        private final Deque<ClientStreamRefreshNotification> pending = new ArrayDeque<>();
        private final Map<String, Long> latestVersions = new HashMap<>();
        private boolean sending;
        private volatile ScheduledFuture<?> heartbeat;

        private RegisteredSubscription(
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

        private synchronized boolean accepts(String resourceName, String resourceId) {
            return subscription.accepts(resourceName, resourceId);
        }

        private synchronized EnqueueResult enqueue(ClientStreamRefreshNotification notification) {
            final long latestVersion = latestVersions.getOrDefault(notification.resourceId(), -1L);
            if (notification.version() <= latestVersion) {
                return EnqueueResult.DUPLICATE;
            }
            if (pending.size() >= maxPendingEvents) {
                return EnqueueResult.OVERFLOW;
            }

            latestVersions.put(notification.resourceId(), notification.version());
            pending.addLast(notification);
            if (!sending) {
                sending = true;
                return EnqueueResult.SCHEDULE;
            }
            return EnqueueResult.QUEUED;
        }

        private synchronized ClientStreamRefreshNotification next() {
            if (pending.isEmpty()) {
                sending = false;
                return null;
            }
            return pending.removeFirst();
        }

        private void cancelHeartbeat() {
            final ScheduledFuture<?> task = heartbeat;
            if (task != null) {
                task.cancel(false);
            }
        }
    }
}
