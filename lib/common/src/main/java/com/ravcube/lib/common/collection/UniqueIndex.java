package com.ravcube.lib.common.collection;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public final class UniqueIndex {

    private UniqueIndex() {
    }

    public static <K, V> Map<K, V> by(
            Collection<? extends V> values,
            Function<? super V, ? extends K> keyExtractor,
            String duplicateMessagePrefix
    ) {
        Objects.requireNonNull(duplicateMessagePrefix, "duplicateMessagePrefix must not be null");
        return by(values, keyExtractor, key -> duplicateMessagePrefix + key);
    }

    public static <K, V> Map<K, V> by(
            Collection<? extends V> values,
            Function<? super V, ? extends K> keyExtractor,
            Function<? super K, String> duplicateMessageFactory
    ) {
        requireArguments(values, keyExtractor, duplicateMessageFactory);
        return Collections.unmodifiableMap(buildIndex(values, keyExtractor, duplicateMessageFactory));
    }

    private static <K, V> void requireArguments(
            Collection<? extends V> values,
            Function<? super V, ? extends K> keyExtractor,
            Function<? super K, String> duplicateMessageFactory
    ) {
        Objects.requireNonNull(values, "values must not be null");
        Objects.requireNonNull(keyExtractor, "keyExtractor must not be null");
        Objects.requireNonNull(duplicateMessageFactory, "duplicateMessageFactory must not be null");
    }

    private static <K, V> Map<K, V> buildIndex(
            Collection<? extends V> values,
            Function<? super V, ? extends K> keyExtractor,
            Function<? super K, String> duplicateMessageFactory
    ) {
        Map<K, V> index = new LinkedHashMap<>(capacityFor(values.size()));
        for (V value : values) {
            V entry = Objects.requireNonNull(value, "values must not contain null");
            K key = Objects.requireNonNull(keyExtractor.apply(entry), "indexed key must not be null");
            if (index.putIfAbsent(key, entry) != null) {
                throw new IllegalArgumentException(duplicateMessageFactory.apply(key));
            }
        }
        return index;
    }

    private static int capacityFor(int size) {
        return Math.max(16, (int) (size / 0.75f) + 1);
    }
}
