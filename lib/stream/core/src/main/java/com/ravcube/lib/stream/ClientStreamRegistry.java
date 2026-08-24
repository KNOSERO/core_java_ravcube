package com.ravcube.lib.stream;

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
import java.util.function.Predicate;

@Component
public final class ClientStreamRegistry {

    public static final String REFRESH_EVENT = "refresh";

    private final Duration timeout;
    private final CopyOnWriteArrayList<Subscription> subscriptions = new CopyOnWriteArrayList<>();

    public ClientStreamRegistry(ClientStreamProperties properties) {
        this.timeout = Objects.requireNonNull(properties, "properties must not be null").timeout();
    }

    public SseEmitter subscribeResource(String resourceName, String resourceId) {
        return register(Subscription.resource(resourceName, resourceId));
    }

    public SseEmitter subscribeCollection(String resourceName) {
        return register(Subscription.collection(resourceName));
    }

    public SseEmitter subscribeSelectedCollection(String resourceName, Collection<String> resourceIds) {
        return register(Subscription.selectedCollection(resourceName, resourceIds));
    }

    public void publishResource(String resourceName, String resourceId, Object payload) {
        final String validatedResourceName = ClientStreamNames.collection(resourceName);
        final String validatedResourceId = requireText(resourceId, "resourceId");
        publish(
                subscription -> subscription.acceptsResource(validatedResourceName, validatedResourceId),
                payload
        );
    }

    public void publishCollection(String resourceName, Object payload) {
        final String validatedResourceName = ClientStreamNames.collection(resourceName);
        publish(
                subscription -> subscription.acceptsCollection(validatedResourceName),
                payload
        );
    }

    public void publishSelectedCollection(
            String resourceName,
            Collection<String> resourceIds,
            Object payload
    ) {
        final String validatedResourceName = ClientStreamNames.collection(resourceName);
        final Set<String> validatedResourceIds = normalizeIds(resourceIds);
        publish(
                subscription -> subscription.acceptsSelectedCollection(validatedResourceName, validatedResourceIds),
                payload
        );
    }

    public void sendInitial(SseEmitter emitter, Object payload) {
        send(Objects.requireNonNull(emitter, "emitter must not be null"), payload);
    }

    private SseEmitter register(Subscription subscription) {
        final SseEmitter emitter = new SseEmitter(timeout.toMillis());
        final Subscription registered = subscription.withEmitter(emitter);
        subscriptions.add(registered);

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

    int activeSubscriptions() {
        return subscriptions.size();
    }

    private static Set<String> normalizeIds(Collection<String> resourceIds) {
        Objects.requireNonNull(resourceIds, "resourceIds must not be null");
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

    private enum SubscriptionType {
        RESOURCE,
        COLLECTION,
        SELECTED_COLLECTION
    }

    private record Subscription(
            String resourceName,
            SubscriptionType type,
            String resourceId,
            Set<String> resourceIds,
            SseEmitter emitter
    ) {

        static Subscription resource(String resourceName, String resourceId) {
            return new Subscription(
                    ClientStreamNames.collection(resourceName),
                    SubscriptionType.RESOURCE,
                    requireText(resourceId, "resourceId"),
                    Set.of(),
                    null
            );
        }

        static Subscription collection(String resourceName) {
            return new Subscription(
                    ClientStreamNames.collection(resourceName),
                    SubscriptionType.COLLECTION,
                    null,
                    Set.of(),
                    null
            );
        }

        static Subscription selectedCollection(String resourceName, Collection<String> resourceIds) {
            return new Subscription(
                    ClientStreamNames.collection(resourceName),
                    SubscriptionType.SELECTED_COLLECTION,
                    null,
                    normalizeIds(resourceIds),
                    null
            );
        }

        Subscription withEmitter(SseEmitter registeredEmitter) {
            return new Subscription(resourceName, type, resourceId, resourceIds, registeredEmitter);
        }

        boolean acceptsResource(String name, String id) {
            return resourceName.equals(name)
                    && (type == SubscriptionType.COLLECTION
                    || type == SubscriptionType.RESOURCE && id.equals(resourceId)
                    || type == SubscriptionType.SELECTED_COLLECTION && resourceIds.contains(id));
        }

        boolean acceptsCollection(String name) {
            return type == SubscriptionType.COLLECTION && resourceName.equals(name);
        }

        boolean acceptsSelectedCollection(String name, Set<String> ids) {
            return type == SubscriptionType.SELECTED_COLLECTION
                    && resourceName.equals(name)
                    && resourceIds.equals(ids);
        }
    }
}
