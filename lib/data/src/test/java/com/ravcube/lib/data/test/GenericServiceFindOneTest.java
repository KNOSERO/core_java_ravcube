package com.ravcube.lib.data.test;

import com.ravcube.lib.data.entity.TestItem;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.ravcube.lib.data.test.GenericServiceContainerTestBase.TestItemFixtures.testItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class GenericServiceFindOneTest extends GenericServiceContainerTestBase {

    @Test
    void shouldReturnEntityForMatchingPredicate() {
        // given
        TestItem entity = testItem("alpha");
        TestItem saved = service.create(entity);

        // when
        Optional<TestItem> result = service.findOne(q -> q.name.eq("alpha"));

        // then
        assertTrue(result.isPresent());
        assertEquals(saved.getId(), result.orElseThrow().getId());
    }

    @Test
    void shouldReturnEmptyForNonMatchingPredicate() {
        // given
        TestItem entity = testItem("alpha");
        service.create(entity);

        // when
        Optional<TestItem> result = service.findOne(q -> q.name.eq("beta"));

        // then
        assertTrue(result.isEmpty());
    }
}
