package com.ravcube.lib.stream.infrastructure.sse;

import com.ravcube.lib.stream.api.ClientStreamAuthorization;
import com.ravcube.lib.stream.application.ClientStreamAccessDeniedException;
import com.ravcube.lib.stream.application.ClientStreamLimitExceededException;
import com.ravcube.lib.stream.domain.ClientStreamSubscription;
import com.ravcube.lib.stream.infrastructure.config.ClientStreamProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.LongFunction;
import java.util.function.Predicate;

@Component
public final class ClientStreamRegistry {

    public static final String REFRESH_EVENT = "refresh";

    private final Duration timeout;
    private final int maxIdsPerSubscription;
    private final int maxSubscriptions;
    private final ClientStreamAuthorization authorization;
    private final LongFunction<SseEmitter> emitterFactory;
    private final CopyOnWriteArrayList<RegisteredSubscription> subscriptions = new CopyOnWriteArrayList<>();

    public ClientStreamRegistry(
            ClientStreamProperties properties,
            ClientStreamAuthorization authorization
    ) {
        this(properties, authorization, timeout -> new SseEmitter(timeout));
    }

    ClientStreamRegistry(
            ClientStreamProperties properties,
            ClientStreamAuthorization authorization,
            LongFunction<SseEmitter> emitterFactory
    ) {
        final ClientStreamProperties validatedProperties = Objects.requireNonNull(
                properties,
                "properties must not be null"
        );
        this.timeout = validatedProperties.timeout();
        this.maxIdsPerSubscription = validatedProperties.maxIdsPerSubscription();
        this.maxSubscriptions = validatedProperties.maxSubscriptions();
        this.authorization = Objects.requireNonNull(
                authorization,
                "authorization must not be null"
        );
        this.emitterFactory = Objects.requireNonNull(
                emitterFactory,
                "emitterFactory must not be null"
        );
    }

    public SseEmitter subscribe(String resourceName, Collection<String> resourceIds) {
        final String validatedResourceName = requireText(resourceName, "resourceName");
        final Set<String> normalizedIds = normalizeIds(resourceIds, maxIdsPerSubscription);
        for (String resourceId : normalizedIds) {
            assertAuthorized(validatedResourceName, resourceId);
        }

        return register(new ClientStreamSubscription(validatedResourceName, normalizedIds));
    }

    public void publish(String resourceName, String resourceId, Object payload) {
        final String validatedResourceName = requireText(resourceName, "resourceName");
        final String validatedResourceId = requireText(resourceId, "resourceId");

        if (!isAuthorized(validatedResourceName, validatedResourceId)) {
            return;
        }

        publish(
                subscription -> subscription.accepts(validatedResourceName, validatedResourceId),
                payload
        );
    }

    private void assertAuthorized(String resourceName, String resourceId) {
        final String validatedResourceName = requireText(resourceName, "resourceName");
        final String validatedResourceId = requireText(resourceId, "resourceId");
        if (!isAuthorized(validatedResourceName, validatedResourceId)) {
            throw new ClientStreamAccessDeniedException(validatedResourceName, validatedResourceId);
        }
    }

    public void sendInitial(
            SseEmitter emitter,
            String resourceName,
            String resourceId,
            Object payload
    ) {
        final SseEmitter validatedEmitter = Objects.requireNonNull(
                emitter,
                "emitter must not be null"
        );
        final RegisteredSubscription subscription = subscriptions.stream()
                .filter(candidate -> candidate.emitter() == validatedEmitter)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("emitter is not registered"));

        final String validatedResourceName = requireText(resourceName, "resourceName");
        final String validatedResourceId = requireText(resourceId, "resourceId");
        if (!subscription.accepts(validatedResourceName, validatedResourceId)
                || !isAuthorized(validatedResourceName, validatedResourceId)) {
            throw new ClientStreamAccessDeniedException(validatedResourceName, validatedResourceId);
        }

        send(validatedEmitter, Objects.requireNonNull(payload, "payload must not be null"));
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

    private void publish(Predicate<RegisteredSubscription> selector, Object payload) {
        Objects.requireNonNull(payload, "payload must not be null");
        subscriptions.stream()
                .filter(selector)
                .forEach(subscription -> send(subscription.emitter(), payload));
    }

    private boolean isAuthorized(String resourceName, String resourceId) {
        return authorization.canRead(resourceName, resourceId);
    }

    private void send(SseEmitter emitter, Object payload) {
        try {
            synchronized (emitter) {
                emitter.send(SseEmitter.event().name(REFRESH_EVENT).data(payload));
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

    private static Set<String> normalizeIds(
            Collection<String> resourceIds,
            int maxIdsPerSubscription
    ) {
        Objects.requireNonNull(resourceIds, "resourceIds must not be null");

        if (resourceIds.size() > maxIdsPerSubscription) {
            throw new ClientStreamLimitExceededException(
                    "Maximum number of ids per stream subscription is " + maxIdsPerSubscription
            );
        }

        final TreeSet<String> normalizedIds = new TreeSet<>();
        for (String resourceId : resourceIds) {
            normalizedIds.add(requireText(resourceId, "resourceId"));
        }

        if (normalizedIds.isEmpty()) {
            throw new IllegalArgumentException("resourceIds must not be empty");
        }

        return Collections.unmodifiableSet(normalizedIds);
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
