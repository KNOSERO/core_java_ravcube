package com.ravcube.lib.data.test;

import com.ravcube.lib.data.entity.TestItem;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static com.ravcube.lib.data.test.GenericServiceContainerTestBase.TestItemFixtures.testItem;
import static org.junit.jupiter.api.Assertions.assertEquals;


class GenericServiceFindPageTest extends GenericServiceContainerTestBase {

    @Test
    void shouldReturnPageWithExpectedContent() {
        // given
        TestItem first = testItem("a");
        service.create(first);
        TestItem second = testItem("b");
        service.create(second);
        TestItem third = testItem("c");
        service.create(third);
        Pageable pageable = PageRequest.of(0, 2);

        // when
        Page<TestItem> result = service.findPage(q -> q.name.isNotNull(), pageable);

        // then
        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalElements());
    }
}
