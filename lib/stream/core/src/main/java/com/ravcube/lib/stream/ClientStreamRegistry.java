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
import java.util.function.LongFunction;
import java.util.function.Predicate;

@Component
public final class ClientStreamRegistry {

    public static final String REFRESH_EVENT = "refresh";

    private final Duration timeout;
    private final LongFunction<SseEmitter> emitterFactory;
    private final CopyOnWriteArrayList<Subscription> subscriptions = new CopyOnWriteArrayList<>();

    public ClientStreamRegistry(ClientStreamProperties properties) {
        this(properties, timeout -> new SseEmitter(timeout));
    }

    ClientStreamRegistry(
            ClientStreamProperties properties,
            LongFunction<SseEmitter> emitterFactory
    ) {
        this.timeout = Objects.requireNonNull(properties, "properties must not be null").timeout();
        this.emitterFactory = Objects.requireNonNull(
                emitterFactory,
                "emitterFactory must not be null"
        );
    }

    public SseEmitter subscribe(String resourceName, Collection<String> resourceIds) {
        return register(Subscription.create(resourceName, resourceIds));
    }

    public void publish(String resourceName, String resourceId, Object payload) {
        final String validatedResourceName = requireText(resourceName, "resourceName");
        final String validatedResourceId = requireText(resourceId, "resourceId");

        publish(
                subscription -> subscription.accepts(validatedResourceName, validatedResourceId),
                payload
        );
    }

    public void sendInitial(SseEmitter emitter, Object payload) {
        send(Objects.requireNonNull(emitter, "emitter must not be null"), payload);
    }

    private SseEmitter register(Subscription subscription) {
        final SseEmitter emitter = Objects.requireNonNull(
                emitterFactory.apply(timeout.toMillis()),
                "emitterFactory returned null"
        );
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

    private record Subscription(
            String resourceName,
            Set<String> resourceIds,
            SseEmitter emitter
    ) {

        static Subscription create(String resourceName, Collection<String> resourceIds) {
            return new Subscription(
                    requireText(resourceName, "resourceName"),
                    normalizeIds(resourceIds),
                    null
            );
        }

        Subscription withEmitter(SseEmitter registeredEmitter) {
            return new Subscription(resourceName, resourceIds, registeredEmitter);
        }

        boolean accepts(String name, String id) {
            return resourceName.equals(name) && resourceIds.contains(id);
        }
    }
}
