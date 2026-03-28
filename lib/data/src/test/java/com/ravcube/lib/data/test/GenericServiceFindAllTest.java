package com.ravcube.lib.data.test;

import com.querydsl.core.types.OrderSpecifier;
import com.ravcube.lib.data.entity.SpecializedTestItem;
import com.ravcube.lib.data.entity.QSpecializedTestItem;
import com.ravcube.lib.data.entity.QTestItem;
import com.ravcube.lib.data.entity.TestItem;
import com.ravcube.lib.data.entity.TestItemEntry;
import com.ravcube.lib.data.view.PolicyDto;
import com.ravcube.lib.data.view.TestItemView;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.StreamSupport;

import static com.ravcube.lib.data.test.GenericServiceContainerTestBase.TestItemFixtures.specializedItem;
import static com.ravcube.lib.data.test.GenericServiceContainerTestBase.TestItemFixtures.testItem;
import static com.ravcube.lib.data.test.GenericServiceContainerTestBase.TestItemFixtures.testItemEntry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class GenericServiceFindAllTest extends GenericServiceContainerTestBase {

    @Test
    void shouldReturnAllMatchingEntities() {
        // given
        TestItem first = testItem("alpha");
        service.create(first);
        TestItem second = testItem("beta");
        service.create(second);
        TestItem third = testItem("gamma");
        service.create(third);

        // when
        List<TestItem> result = service.findAll(q -> q.name.in("alpha", "beta"));

        // then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(e -> "alpha".equals(e.getName())));
        assertTrue(result.stream().anyMatch(e -> "beta".equals(e.getName())));
    }

    @Test
    void shouldReturnEmptyListForNonMatchingPredicate() {
        // given
        TestItem entity = testItem("alpha");
        service.create(entity);

        // when
        List<TestItem> result = service.findAll(q -> q.name.eq("missing"));

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnProjectedViews() {
        // given
        TestItem first = testItem("alpha");
        service.create(first);
        TestItem second = testItem("beta");
        service.create(second);

        // when
        List<TestItemView> result = service.findAll(q -> q.name.in("alpha", "beta"), TestItemView.class);

        // then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(v -> "alpha".equals(v.getName())));
        assertTrue(result.stream().anyMatch(v -> "beta".equals(v.getName())));
    }

    @Test
    void shouldReturnEmptyProjectedViewListWhenNoMatch() {
        // given
        TestItem entity = testItem("alpha");
        service.create(entity);

        // when
        List<TestItemView> result = service.findAll(q -> q.name.eq("missing"), TestItemView.class);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnSortedEntities() {
        // given
        TestItem first = testItem("beta");
        service.create(first);
        TestItem second = testItem("alpha");
        service.create(second);
        Sort sort = Sort.by(Sort.Order.asc("id"));

        // when
        List<TestItem> result = StreamSupport
                .stream(service.findAll(q -> q.name.isNotNull(), sort).spliterator(), false)
                .toList();

        // then
        assertEquals(2, result.size());
        assertEquals("beta", result.get(0).getName());
        assertEquals("alpha", result.get(1).getName());
    }

    @Test
    void shouldReturnEntitiesOrderedByOrderSpecifiers() {
        // given
        TestItem first = testItem("alpha");
        service.create(first);
        TestItem second = testItem("gamma");
        service.create(second);
        TestItem third = testItem("beta");
        service.create(third);
        OrderSpecifier<?> orderByNameAsc = QTestItem.testItem.name.asc();

        // when
        List<TestItem> result = StreamSupport
                .stream(service.findAll(q -> q.name.isNotNull(), orderByNameAsc).spliterator(), false)
                .toList();

        // then
        assertEquals(3, result.size());
        assertEquals("alpha", result.get(0).getName());
        assertEquals("beta", result.get(1).getName());
        assertEquals("gamma", result.get(2).getName());
    }

    @Test
    void shouldFilterChildEntityUsingTreatAndReturnPolicyDto() {
        // given
        TestItem policyEntity = testItem("base");
        TestItem policy = service.create(policyEntity);
        SpecializedTestItem specializedMatch = specializedItem("specialized-match", policy.getId());
        service.create(specializedMatch);
        SpecializedTestItem specializedOther = specializedItem("specialized-other", -1L);
        service.create(specializedOther);

        // when
        List<PolicyDto> result = service.findAll(path -> {
            QSpecializedTestItem specializedPath = service.treat(path, QSpecializedTestItem.class);
            return specializedPath.byId.eq(policy.getId());
        }, PolicyDto.class);

        // then
        assertEquals(1, result.size());
        assertEquals("specialized-match", result.get(0).getName());
    }

    @Test
    void shouldFilterByOneToManyChildAttribute() {
        // given
        TestItem first = testItem("with-target-entry");
        TestItemEntry firstEntry = testItemEntry("target");
        first.addEntry(firstEntry);
        service.create(first);

        TestItem second = testItem("without-target-entry");
        TestItemEntry secondEntry = testItemEntry("other");
        second.addEntry(secondEntry);
        service.create(second);

        // when
        List<TestItem> result = service.findAll(q -> q.entries.any().value.eq("target"));

        // then
        assertEquals(1, result.size());
        assertEquals("with-target-entry", result.get(0).getName());
    }
}
