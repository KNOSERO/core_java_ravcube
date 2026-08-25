package com.ravcube.lib.stream.domain;

import com.ravcube.lib.stream.api.ClientStreamAccess;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public record ClientStreamSubscription(
        String resourceName,
        Set<String> resourceIds,
        ClientStreamAccess access
) {

    public ClientStreamSubscription {
        resourceName = requireText(resourceName, "resourceName");
        resourceIds = normalizeIds(resourceIds);
        access = Objects.requireNonNull(access, "access must not be null");
    }

    public boolean accepts(String name, String id) {
        return resourceName.equals(name)
                && resourceIds.contains(id)
                && access.allows(id);
    }

    private static Set<String> normalizeIds(Set<String> values) {
        Objects.requireNonNull(values, "resourceIds must not be null");
        final TreeSet<String> normalized = new TreeSet<>();
        for (String value : values) {
            normalized.add(requireText(value, "resourceId"));
        }
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("resourceIds must not be empty");
        }
        return Collections.unmodifiableSet(normalized);
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
