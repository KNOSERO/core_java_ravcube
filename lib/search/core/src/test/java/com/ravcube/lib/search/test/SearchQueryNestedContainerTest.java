package com.ravcube.lib.search.test;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SearchQueryNestedContainerTest extends AbstractSearchServiceContainerTest {

    @Test
    void shouldFilterByNestedTerm() throws InterruptedException {
        // given
        service.createAll(List.of(
                document("doc-1", "First", 1, List.of(
                        relation("team", "backendteam"),
                        relation("owner", "ravcube")
                )),
                document("doc-2", "Second", 1, List.of(
                        relation("team", "frontendteam"),
                        relation("owner", "rafal")
                ))
        ));

        // when
        waitUntilCount(q -> q.in("relations").term("value", "backendteam"), 1, Duration.ofSeconds(5));
        long count = service.count(q -> q.in("relations").term("value", "backendteam"));

        // then
        assertEquals(1, count);
    }

    @Test
    void shouldFilterByNestedMatch() throws InterruptedException {
        // given
        service.createAll(List.of(
                document("doc-1", "First", 1, List.of(relation("role", "platform backend team"))),
                document("doc-2", "Second", 1, List.of(relation("role", "platform frontend team")))
        ));

        // when
        waitUntilCount(q -> q.in("relations").match("value", "backend"), 1, Duration.ofSeconds(5));
        long count = service.count(q -> q.in("relations").match("value", "backend"));

        // then
        assertEquals(1, count);
    }

    @Test
    void shouldFilterByNestedWildcard() throws InterruptedException {
        // given
        service.createAll(List.of(
                document("doc-1", "First", 1, List.of(relation("role", "backendteam"))),
                document("doc-2", "Second", 1, List.of(relation("role", "frontendteam")))
        ));

        // when
        waitUntilCount(q -> q.in("relations").wildcard("value", "*front*"), 1, Duration.ofSeconds(5));
        long count = service.count(q -> q.in("relations").wildcard("value", "*front*"));

        // then
        assertEquals(1, count);
    }

    @Test
    void shouldFilterByTwoNestedListsAtTheSameTime() throws InterruptedException {
        // given
        service.createAll(List.of(
                document("doc-1", "First", 1,
                        List.of(relation("team", "backendteam")),
                        List.of(relation("tier", "vip"))),
                document("doc-2", "Second", 1,
                        List.of(relation("team", "backendteam")),
                        List.of(relation("tier", "basic"))),
                document("doc-3", "Third", 1,
                        List.of(relation("team", "frontendteam")),
                        List.of(relation("tier", "vip")))
        ));

        // when
        waitUntilCount(q -> q.in("relations").term("value", "backendteam")
                .must(q.in("labels").term("value", "vip")), 1, Duration.ofSeconds(5));

        long count = service.count(q -> q.in("relations").term("value", "backendteam")
                .must(q.in("labels").term("value", "vip")));

        // then
        assertEquals(1, count);
    }

    @Test
    void shouldFilterByDoubleInScope() throws InterruptedException {
        // given
        service.createAll(List.of(
                document("doc-1", "First", 1, List.of(
                        relation("team", "backend", List.of(
                                relationDetail("tech", "java"),
                                relationDetail("tech", "kotlin")
                        ))
                )),
                document("doc-2", "Second", 1, List.of(
                        relation("team", "frontend", List.of(
                                relationDetail("tech", "typescript")
                        ))
                ))
        ));

        // when
        waitUntilCount(q -> q.in("relations").in("details").term("value", "java"), 1, Duration.ofSeconds(5));
        long count = service.count(q -> q.in("relations").in("details").term("value", "java"));

        // then
        assertEquals(1, count);
    }
}
