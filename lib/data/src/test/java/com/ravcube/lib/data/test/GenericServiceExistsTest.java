package com.ravcube.lib.data.test;

import com.ravcube.lib.data.entity.TestItem;
import org.junit.jupiter.api.Test;

import static com.ravcube.lib.data.test.GenericServiceContainerTestBase.TestItemFixtures.testItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class GenericServiceExistsTest extends GenericServiceContainerTestBase {

    @Test
    void shouldCountMatchingEntities() {
        // given
        TestItem first = testItem("alpha");
        service.create(first);
        TestItem second = testItem("beta");
        service.create(second);
        TestItem third = testItem("gamma");
        service.create(third);

        // when
        long result = service.count(q -> q.name.in("alpha", "beta"));

        // then
        assertEquals(2L, result);
    }

    @Test
    void shouldReturnTrueWhenAnyEntityMatches() {
        // given
        TestItem entity = testItem("alpha");
        service.create(entity);

        // when
        boolean result = service.exists(q -> q.name.eq("alpha"));

        // then
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenNoEntityMatches() {
        // given
        TestItem entity = testItem("alpha");
        service.create(entity);

        // when
        boolean result = service.exists(q -> q.name.eq("missing"));

        // then
        assertFalse(result);
    }
}
