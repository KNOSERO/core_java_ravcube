package com.ravcube.lib.stream;

import java.util.Collection;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

public final class ClientStreamNames {

    private ClientStreamNames() {
    }

    public static String collection(String resourceName) {
        return requireText(resourceName, "resourceName");
    }

    public static String resource(String resourceName, String resourceId) {
        return collection(resourceName) + "." + requireText(resourceId, "resourceId");
    }

    public static String selectedCollection(String resourceName, Collection<String> resourceIds) {
        Objects.requireNonNull(resourceIds, "resourceIds must not be null");

        final TreeSet<String> normalizedIds = resourceIds.stream()
                .map(resourceId -> requireText(resourceId, "resourceId"))
                .collect(Collectors.toCollection(TreeSet::new));

        if (normalizedIds.isEmpty()) {
            throw new IllegalArgumentException("resourceIds must not be empty");
        }

        return collection(resourceName) + "." + String.join(",", normalizedIds);
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
