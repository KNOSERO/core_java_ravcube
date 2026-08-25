package com.ravcube.lib.stream.infrastructure.sse;

import com.ravcube.lib.logger.Logger;
import com.ravcube.lib.logger.LoggerFactory;
import com.ravcube.lib.stream.domain.ClientStreamSubscription;
import com.ravcube.lib.stream.infrastructure.config.ClientStreamProperties;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.LongFunction;

@Component
public final class ClientStreamRegistry implements DisposableBean {

    public static final String REFRESH_EVENT = "refresh";

    private final Duration timeout;
    private final int maxIdsPerSubscription;
    private final int maxSubscriptions;
    private final int maxPendingEventsPerSubscription;
    private final LongFunction<SseEmitter> emitterFactory;
    private final Executor sender;
    private final ExecutorService ownedSender;
    private final Logger logger;
    private final java.util.concurrent.CopyOnWriteArrayList<RegisteredSubscription> subscriptions =
            new java.util.concurrent.CopyOnWriteArrayList<>();

    @Autowired
    public ClientStreamRegistry(
            ClientStreamProperties properties,
            LoggerFactory loggerFactory
    ) {
        this(
                properties,
                timeout -> new SseEmitter(timeout),
                createSender(properties),
                true,
                loggerFactory.getLogger(ClientStreamRegistry.class)
        );
    }

    ClientStreamRegistry(
            ClientStreamProperties properties,
            LongFunction<SseEmitter> emitterFactory
    ) {
        this(properties, emitterFactory, Runnable::run, false, Logger.noop());
    }

    ClientStreamRegistry(
            ClientStreamProperties properties,
            LongFunction<SseEmitter> emitterFactory,
            Executor sender
    ) {
        this(properties, emitterFactory, sender, false, Logger.noop());
    }

    private ClientStreamRegistry(
            ClientStreamProperties properties,
            LongFunction<SseEmitter> emitterFactory,
            Executor sender,
            boolean ownsSender,
            Logger logger
    ) {
        final ClientStreamProperties validatedProperties = Objects.requireNonNull(
                properties,
                "properties must not be null"
        );
        this.timeout = validatedProperties.timeout();
        this.maxIdsPerSubscription = validatedProperties.maxIdsPerSubscription();
        this.maxSubscriptions = validatedProperties.maxSubscriptions();
        this.maxPendingEventsPerSubscription =
                validatedProperties.maxPendingEventsPerSubscription();
        this.emitterFactory = Objects.requireNonNull(
                emitterFactory,
                "emitterFactory must not be null"
        );
        this.sender = Objects.requireNonNull(sender, "sender must not be null");
        this.ownedSender = ownsSender ? (ExecutorService) sender : null;
        this.logger = Objects.requireNonNull(logger, "logger must not be null");
    }

    public SseEmitter subscribe(String resourceName, Collection<String> resourceIds) {
        final String validatedResourceName = requireText(resourceName, "resourceName");
        final Set<String> validatedIds = validateIds(resourceIds, maxIdsPerSubscription);
        final ClientStreamSubscription subscription = new ClientStreamSubscription(
                validatedResourceName,
                validatedIds
        );

        final SseEmitter emitter;
        final RegisteredSubscription registered;
        synchronized (subscriptions) {
            if (subscriptions.size() >= maxSubscriptions) {
                throw new ClientStreamLimitExceededException(
                        "Maximum number of stream subscriptions has been reached"
                );
            }

            emitter = Objects.requireNonNull(
                    emitterFactory.apply(timeout.toMillis()),
                    "emitterFactory returned null"
            );
            registered = new RegisteredSubscription(
                    subscription,
                    emitter,
                    maxPendingEventsPerSubscription
            );
            subscriptions.add(registered);
        }

        emitter.onCompletion(() -> remove(emitter));
        emitter.onTimeout(() -> {
            remove(emitter);
            emitter.complete();
        });
        emitter.onError(error -> remove(emitter));

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

    private void removeSlowSubscription(RegisteredSubscription subscription) {
        logger.warn(
                "SSE subscription removed after reaching the pending event limit for resource {}",
                subscription.subscription.resourceName
        );
        remove(subscription.emitter);
        subscription.emitter.completeWithError(
                new IOException("SSE subscriber is too slow")
        );
    }

    private static ExecutorService createSender(ClientStreamProperties properties) {
        final int threads = Math.min(8, Math.max(2, properties.maxSubscriptions()));
        return new java.util.concurrent.ThreadPoolExecutor(
                threads,
                threads,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(properties.maxSubscriptions()),
                Executors.defaultThreadFactory(),
                new java.util.concurrent.ThreadPoolExecutor.AbortPolicy()
        );
    }

    private void remove(SseEmitter emitter) {
        subscriptions.removeIf(subscription -> subscription.emitter == emitter);
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
    }

    private static Set<String> validateIds(
            Collection<String> resourceIds,
            int maxIdsPerSubscription
    ) {
        Objects.requireNonNull(resourceIds, "resourceIds must not be null");

        if (resourceIds.size() > maxIdsPerSubscription) {
            throw new ClientStreamLimitExceededException(
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
        private final int maxPendingEvents;
        private final Deque<ClientStreamRefreshNotification> pending = new ArrayDeque<>();
        private final Map<String, Long> latestVersions = new HashMap<>();
        private boolean sending;

        private RegisteredSubscription(
                ClientStreamSubscription subscription,
                SseEmitter emitter,
                int maxPendingEvents
        ) {
            this.subscription = subscription;
            this.emitter = emitter;
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
    }
}
