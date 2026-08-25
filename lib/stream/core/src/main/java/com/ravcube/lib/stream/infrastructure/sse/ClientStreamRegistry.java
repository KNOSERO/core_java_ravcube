package com.ravcube.lib.stream.infrastructure.sse;

import com.ravcube.lib.stream.api.ClientStreamRefreshNotification;
import com.ravcube.lib.stream.application.ClientStreamLimitExceededException;
import com.ravcube.lib.stream.domain.ClientStreamSubscription;
import com.ravcube.lib.stream.infrastructure.config.ClientStreamProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.LongFunction;
import java.util.function.Predicate;

@Component
public final class ClientStreamRegistry {

    public static final String REFRESH_EVENT = "refresh";

    private final Duration timeout;
    private final int maxIdsPerSubscription;
    private final int maxSubscriptions;
    private final LongFunction<SseEmitter> emitterFactory;
    private final CopyOnWriteArrayList<RegisteredSubscription> subscriptions = new CopyOnWriteArrayList<>();

    public ClientStreamRegistry(ClientStreamProperties properties) {
        this(properties, timeout -> new SseEmitter(timeout));
    }

    ClientStreamRegistry(
            ClientStreamProperties properties,
            LongFunction<SseEmitter> emitterFactory
    ) {
        final ClientStreamProperties validatedProperties = Objects.requireNonNull(
                properties,
                "properties must not be null"
        );
        this.timeout = validatedProperties.timeout();
        this.maxIdsPerSubscription = validatedProperties.maxIdsPerSubscription();
        this.maxSubscriptions = validatedProperties.maxSubscriptions();
        this.emitterFactory = Objects.requireNonNull(
                emitterFactory,
                "emitterFactory must not be null"
        );
    }

    public SseEmitter subscribe(String resourceName, Collection<String> resourceIds) {
        final String validatedResourceName = requireText(resourceName, "resourceName");
        final Set<String> validatedIds = validateIds(resourceIds, maxIdsPerSubscription);
        final ClientStreamSubscription subscription = new ClientStreamSubscription(
                validatedResourceName,
                validatedIds
        );

        return register(subscription);
    }

    public void publish(String resourceName, String resourceId, long version) {
        final String validatedResourceName = requireText(resourceName, "resourceName");
        final ClientStreamRefreshNotification notification =
                new ClientStreamRefreshNotification(resourceId, version);

        publish(
                subscription -> subscription.accepts(
                        validatedResourceName,
                        notification.resourceId()
                ),
                notification
        );
    }

    private SseEmitter register(ClientStreamSubscription subscription) {
        final SseEmitter emitter = Objects.requireNonNull(
                emitterFactory.apply(timeout.toMillis()),
                "emitterFactory returned null"
        );
        final RegisteredSubscription registered = RegisteredSubscription.of(subscription, emitter);
        synchronized (subscriptions) {
            if (subscriptions.size() >= maxSubscriptions) {
                throw new ClientStreamLimitExceededException(
                        "Maximum number of stream subscriptions has been reached"
                );
            }
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

    private void publish(
            Predicate<RegisteredSubscription> selector,
            ClientStreamRefreshNotification notification
    ) {
        subscriptions.stream()
                .filter(selector)
                .forEach(subscription -> send(subscription.emitter(), notification));
    }

    private void send(
            SseEmitter emitter,
            ClientStreamRefreshNotification notification
    ) {
        try {
            synchronized (emitter) {
                emitter.send(SseEmitter.event().name(REFRESH_EVENT).data(notification));
            }
        } catch (IOException | IllegalStateException exception) {
            remove(emitter);
            emitter.completeWithError(exception);
        }
    }

    private void remove(SseEmitter emitter) {
        subscriptions.removeIf(subscription -> subscription.emitter() == emitter);
    }

    public void unsubscribe(SseEmitter emitter) {
        remove(Objects.requireNonNull(emitter, "emitter must not be null"));
    }

    int activeSubscriptions() {
        return subscriptions.size();
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

        return Set.copyOf(resourceIds);
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    private record RegisteredSubscription(
            ClientStreamSubscription subscription,
            SseEmitter emitter
    ) {

        static RegisteredSubscription of(
                ClientStreamSubscription subscription,
                SseEmitter emitter
        ) {
            return new RegisteredSubscription(subscription, emitter);
        }

        boolean accepts(String name, String id) {
            try {
                return subscription.accepts(name, id);
            } catch (RuntimeException ignored) {
                return false;
            }
        }
    }
}
