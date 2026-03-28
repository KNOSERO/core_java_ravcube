package com.ravcube.lib.data.test;

import com.ravcube.lib.data.entity.TestItem;
import com.ravcube.lib.data.entity.TestItemEntry;
import org.junit.jupiter.api.Test;

import static com.ravcube.lib.data.test.GenericServiceContainerTestBase.TestItemFixtures.testItem;
import static com.ravcube.lib.data.test.GenericServiceContainerTestBase.TestItemFixtures.testItemEntry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


class GenericServiceDeleteTest extends GenericServiceContainerTestBase {

    @Test
    void shouldDeleteExistingEntityById() {
        // given
        TestItem entity = testItem("to-delete");
        TestItem saved = service.create(entity);

        // when
        service.delete(saved.getId());

        // then
        assertFalse(repository.findById(saved.getId()).isPresent());
    }

    @Test
    void shouldDeleteUsingEntityInstanceId() {
        // given
        TestItem entity = testItem("to-delete");
        TestItem saved = service.create(entity);

        // when
        service.delete(saved);

        // then
        assertFalse(repository.findById(saved.getId()).isPresent());
    }

    @Test
    void shouldCascadeDeleteOneToManyEntries() {
        // given
        TestItem entity = testItem("parent");
        TestItemEntry firstEntry = testItemEntry("one");
        entity.addEntry(firstEntry);
        TestItemEntry secondEntry = testItemEntry("two");
        entity.addEntry(secondEntry);
        TestItem saved = service.create(entity);

        // when
        service.delete(saved.getId());
        Long entriesCount = entityManager.createQuery("select count(e) from TestItemEntry e", Long.class)
                .getSingleResult();

        // then
        assertFalse(repository.findById(saved.getId()).isPresent());
        assertEquals(0L, entriesCount);
    }
}
