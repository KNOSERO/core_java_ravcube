package com.ravcube.lib.common.collection;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UniqueIndexTest {

    @Test
    void shouldReturnMapIndexedByKeyWhenKeysAreUnique() {
        // given
        List<TestValue> values = List.of(
                new TestValue("a", "alpha"),
                new TestValue("b", "beta")
        );

        // when
        Map<String, TestValue> result = UniqueIndex.by(values, TestValue::key, "Duplicate key: ");

        // then
        assertEquals(2, result.size());
        assertEquals("alpha", result.get("a").value());
        assertEquals("beta", result.get("b").value());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenDuplicateKeyExistsAndPrefixMessageIsUsed() {
        // given
        List<TestValue> values = List.of(
                new TestValue("dup", "first"),
                new TestValue("dup", "second")
        );

        // when
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> UniqueIndex.by(values, TestValue::key, "Duplicate key: ")
        );

        // then
        assertEquals("Duplicate key: dup", exception.getMessage());
    }

    @Test
    void shouldUseMessageFromFactoryWhenDuplicateKeyExists() {
        // given
        List<TestValue> values = List.of(
                new TestValue("dup", "first"),
                new TestValue("dup", "second")
        );

        // when
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> UniqueIndex.by(values, TestValue::key, key -> "Duplicated: " + key)
        );

        // then
        assertEquals("Duplicated: dup", exception.getMessage());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenInputContainsNullElement() {
        // given
        List<TestValue> values = Arrays.asList(
                new TestValue("a", "alpha"),
                null
        );

        // when + then
        assertThrows(
                NullPointerException.class,
                () -> UniqueIndex.by(values, TestValue::key, "Duplicate key: ")
        );
    }

    @Test
    void shouldThrowNullPointerExceptionWhenExtractedKeyIsNull() {
        // given
        List<TestValue> values = List.of(new TestValue(null, "alpha"));

        // when + then
        assertThrows(
                NullPointerException.class,
                () -> UniqueIndex.by(values, TestValue::key, "Duplicate key: ")
        );
    }

    @Test
    void shouldReturnUnmodifiableMap() {
        // given
        List<TestValue> values = List.of(new TestValue("a", "alpha"));
        Map<String, TestValue> result = UniqueIndex.by(values, TestValue::key, "Duplicate key: ");

        // when + then
        assertThrows(UnsupportedOperationException.class, () -> result.put("b", new TestValue("b", "beta")));
    }

    private record TestValue(String key, String value) {
    }
}
