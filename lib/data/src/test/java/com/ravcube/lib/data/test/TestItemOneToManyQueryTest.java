package com.ravcube.lib.data.test;

import com.ravcube.lib.data.entity.TestItem;
import com.ravcube.lib.data.entity.TestItemEntry;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static com.ravcube.lib.data.test.TestItemFixtures.testItem;
import static com.ravcube.lib.data.test.TestItemFixtures.testItemEntry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestItemOneToManyQueryTest extends GenericServiceContainerTestBase {

    @Test
    void shouldFindAllItemsByEntryValue() {
        // given
        createItemWithEntry("with-target", "target");
        createItemWithEntry("with-other", "other");

        // when
        List<TestItem> result = service.findAll(q -> q.entries.any().value.eq("target"));

        // then
        assertEquals(1, result.size());
        assertEquals("with-target", result.get(0).getName());
    }

    @Test
    void shouldFindOneItemByEntryValue() {
        // given
        TestItem created = createItemWithEntry("single-target", "target");
        createItemWithEntry("single-other", "other");

        // when
        Optional<TestItem> result = service.findOne(q -> q.entries.any().value.eq("target"));

        // then
        assertTrue(result.isPresent());
        assertEquals(created.getId(), result.orElseThrow().getId());
    }

    @Test
    void shouldCountItemsByEntryValue() {
        // given
        createItemWithEntry("target-1", "target");
        createItemWithEntry("target-2", "target");
        createItemWithEntry("other-1", "other");

        // when
        long result = service.count(q -> q.entries.any().value.eq("target"));

        // then
        assertEquals(2, result);
    }

    @Test
    void shouldReturnExistsForEntryValue() {
        // given
        createItemWithEntry("exists-target", "target");

        // when
        boolean exists = service.exists(q -> q.entries.any().value.eq("target"));
        boolean missing = service.exists(q -> q.entries.any().value.eq("missing"));

        // then
        assertTrue(exists);
        assertFalse(missing);
    }

    @Test
    void shouldFindPageByEntryValue() {
        // given
        createItemWithEntry("target-1", "target");
        createItemWithEntry("target-2", "target");
        createItemWithEntry("other-1", "other");

        // when
        Page<TestItem> result = service.findPage(
                q -> q.entries.any().value.eq("target"),
                PageRequest.of(0, 1)
        );

        // then
        assertEquals(1, result.getContent().size());
        assertEquals(2, result.getTotalElements());
    }

    private TestItem createItemWithEntry(String name, String entryValue) {
        TestItem item = testItem(name);
        TestItemEntry entry = testItemEntry(entryValue);
        item.addEntry(entry);
        return service.create(item);
    }
}
