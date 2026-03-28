package com.ravcube.lib.search.test;

import com.ravcube.lib.search.document.SearchTestDocument;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchQueryExactContainerTest extends AbstractSearchServiceContainerTest {

    @Test
    void shouldFindOneByTermQuery() throws InterruptedException {
        // given
        SearchTestDocument alpha = document("alpha-code", "First Document");
        SearchTestDocument beta = document("beta-code", "Second Document");
        service.createAll(List.of(alpha, beta));

        // when
        waitUntilCount(q -> q.term("code", "alpha-code"), 1, Duration.ofSeconds(5));
        Optional<SearchTestDocument> result = service.findOne(q -> q.term("code", "alpha-code"));

        // then
        assertTrue(result.isPresent());
        assertEquals(alpha.getId(), result.orElseThrow().getId());
    }

    @Test
    void shouldFilterByTermsVarargs() throws InterruptedException {
        // given
        service.createAll(List.of(
                document("alpha", "Alpha"),
                document("beta", "Beta"),
                document("gamma", "Gamma")
        ));

        // when
        waitUntilCount(q -> q.terms("code", "alpha", "beta"), 2, Duration.ofSeconds(5));
        List<SearchTestDocument> result = service.findAll(q -> q.terms("code", "alpha", "beta"));

        // then
        assertEquals(2, result.size());
    }

    @Test
    void shouldFilterByTermsCollection() throws InterruptedException {
        // given
        service.createAll(List.of(
                document("alpha", "Alpha"),
                document("beta", "Beta"),
                document("gamma", "Gamma")
        ));

        // when
        waitUntilCount(q -> q.terms("code", List.of("beta", "gamma")), 2, Duration.ofSeconds(5));
        List<SearchTestDocument> result = service.findAll(q -> q.terms("code", List.of("beta", "gamma")));

        // then
        assertEquals(2, result.size());
    }

    @Test
    void shouldCountExistsAndNotExists() throws InterruptedException {
        // given
        service.createAll(List.of(
                document("code-1", "Doc 1"),
                document("code-2", "Doc 2")
        ));

        // when
        waitUntilCount(q -> q.exists("code"), 2, Duration.ofSeconds(5));
        long existsCount = service.count(q -> q.exists("code"));
        long notExistsCount = service.count(q -> q.notExists("unknownField"));
        boolean exists = service.exists(q -> q.exists("code"));

        // then
        assertEquals(2, existsCount);
        assertEquals(2, notExistsCount);
        assertTrue(exists);
    }
}
