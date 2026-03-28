package com.ravcube.lib.search.test;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SearchQueryRangeContainerTest extends AbstractSearchServiceContainerTest {

    @Test
    void shouldFilterByAllRangePredicates() throws InterruptedException {
        // given
        service.createAll(List.of(
                document("code-10", "Priority 10", 10),
                document("code-20", "Priority 20", 20),
                document("code-30", "Priority 30", 30)
        ));

        // when
        waitUntilCount(q -> q.gte("priority", 10), 3, Duration.ofSeconds(5));
        long gt = service.count(q -> q.gt("priority", 10));
        long gte = service.count(q -> q.gte("priority", 20));
        long lt = service.count(q -> q.lt("priority", 30));
        long lte = service.count(q -> q.lte("priority", 20));
        long between = service.count(q -> q.between("priority", 11, 29));

        // then
        assertEquals(2, gt);
        assertEquals(2, gte);
        assertEquals(2, lt);
        assertEquals(2, lte);
        assertEquals(1, between);
    }
}
