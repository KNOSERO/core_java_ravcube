package com.ravcube.lib.stream;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
    private final ClientStreamAuthorizer authorizer;
    private final LongFunction<SseEmitter> emitterFactory;
    private final CopyOnWriteArrayList<Subscription> subscriptions = new CopyOnWriteArrayList<>();

    public ClientStreamRegistry(
            ClientStreamProperties properties,
            ClientStreamAuthorizer authorizer
    ) {
        this(properties, authorizer, timeout -> new SseEmitter(timeout));
    }

    ClientStreamRegistry(
            ClientStreamProperties properties,
            ClientStreamAuthorizer authorizer,
            LongFunction<SseEmitter> emitterFactory
    ) {
        final ClientStreamProperties validatedProperties = Objects.requireNonNull(
                properties,
                "properties must not be null"
        );
        this.timeout = validatedProperties.timeout();
        this.maxIdsPerSubscription = validatedProperties.maxIdsPerSubscription();
        this.maxSubscriptions = validatedProperties.maxSubscriptions();
        this.authorizer = Objects.requireNonNull(authorizer, "authorizer must not be null");
        this.emitterFactory = Objects.requireNonNull(
                emitterFactory,
                "emitterFactory must not be null"
        );
    }

    public SseEmitter subscribe(String resourceName, Collection<String> resourceIds) {
        final String validatedResourceName = requireText(resourceName, "resourceName");
        final Set<String> normalizedIds = normalizeIds(resourceIds, maxIdsPerSubscription);
        final ClientStreamAccess access = Objects.requireNonNull(
                authorizer.authorize(validatedResourceName, normalizedIds),
                "authorizer returned null access"
        );

        if (normalizedIds.stream().anyMatch(resourceId -> !access.allows(resourceId))) {
            throw new ClientStreamAccessDeniedException(validatedResourceName);
        }

        return register(new Subscription(validatedResourceName, normalizedIds, access, null));
    }

    public void publish(String resourceName, String resourceId, Object payload) {
        final String validatedResourceName = requireText(resourceName, "resourceName");
        final String validatedResourceId = requireText(resourceId, "resourceId");

        publish(
                subscription -> subscription.accepts(validatedResourceName, validatedResourceId),
                payload
        );
    }

    void assertAuthorized(String resourceName, String resourceId) {
        final String validatedResourceName = requireText(resourceName, "resourceName");
        final Set<String> normalizedIds = normalizeIds(List.of(resourceId), maxIdsPerSubscription);
        final String validatedResourceId = normalizedIds.iterator().next();
        final ClientStreamAccess access = Objects.requireNonNull(
                authorizer.authorize(validatedResourceName, normalizedIds),
                "authorizer returned null access"
        );
        if (!access.allows(validatedResourceId)) {
            throw new ClientStreamAccessDeniedException(validatedResourceName);
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
        final Subscription subscription = subscriptions.stream()
                .filter(candidate -> candidate.emitter() == validatedEmitter)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("emitter is not registered"));

        if (!subscription.accepts(
                requireText(resourceName, "resourceName"),
                requireText(resourceId, "resourceId")
        )) {
            throw new ClientStreamAccessDeniedException(resourceName);
        }

        send(validatedEmitter, Objects.requireNonNull(payload, "payload must not be null"));
    }

    private SseEmitter register(Subscription subscription) {
        final SseEmitter emitter = Objects.requireNonNull(
                emitterFactory.apply(timeout.toMillis()),
                "emitterFactory returned null"
        );
        final Subscription registered = subscription.withEmitter(emitter);
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

    private void publish(Predicate<Subscription> selector, Object payload) {
        Objects.requireNonNull(payload, "payload must not be null");
        subscriptions.stream()
                .filter(selector)
                .forEach(subscription -> send(subscription.emitter(), payload));
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

    void unsubscribe(SseEmitter emitter) {
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

    private record Subscription(
            String resourceName,
            Set<String> resourceIds,
            ClientStreamAccess access,
            SseEmitter emitter
    ) {

        Subscription withEmitter(SseEmitter registeredEmitter) {
            return new Subscription(resourceName, resourceIds, access, registeredEmitter);
        }

        boolean accepts(String name, String id) {
            if (!resourceName.equals(name) || !resourceIds.contains(id)) {
                return false;
            }
            try {
                return access.allows(id);
            } catch (RuntimeException ignored) {
                return false;
            }
        }
    }
}
