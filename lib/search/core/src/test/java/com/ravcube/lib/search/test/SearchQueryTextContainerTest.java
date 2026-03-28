package com.ravcube.lib.search.test;

import com.ravcube.lib.search.doc.SearchTestDocument;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchQueryTextContainerTest extends AbstractSearchServiceContainerTest {

    @Test
    void shouldFilterByMatch() throws InterruptedException {
        // given
        service.createAll(List.of(
                document("code-1", "Searchable content one"),
                document("code-2", "Other value")
        ));

        // when
        waitUntilCount(q -> q.match("name", "searchable"), 1, Duration.ofSeconds(5));
        long count = service.count(q -> q.match("name", "searchable"));

        // then
        assertEquals(1, count);
    }

    @Test
    void shouldFilterByMatchPhrase() throws InterruptedException {
        // given
        service.createAll(List.of(
                document("code-1", "Fast brown fox"),
                document("code-2", "Fast fox brown")
        ));

        // when
        waitUntilCount(q -> q.matchPhrase("name", "fast brown"), 1, Duration.ofSeconds(5));
        long count = service.count(q -> q.matchPhrase("name", "fast brown"));

        // then
        assertEquals(1, count);
    }

    @Test
    void shouldFilterByPrefixAndWildcard() throws InterruptedException {
        // given
        service.createAll(List.of(
                document("pref-100", "Alpha"),
                document("pref-200", "Beta"),
                document("other-300", "Gamma")
        ));

        // when
        waitUntilCount(q -> q.prefix("code", "pref-"), 2, Duration.ofSeconds(5));
        long prefixCount = service.count(q -> q.prefix("code", "pref-"));
        long wildcardCount = service.count(q -> q.wildcard("code", "pref-*"));

        // then
        assertEquals(2, prefixCount);
        assertEquals(2, wildcardCount);
    }

    @Test
    void shouldFilterByWildcardAndReturnPagedResults() throws InterruptedException {
        // given
        service.createAll(List.of(
                document("search-1", "Searchable entry one"),
                document("search-2", "Searchable entry two"),
                document("other-1", "Other entry")
        ));

        // when
        waitUntilCount(q -> q.wildcard("name", "*searchable*"), 2, Duration.ofSeconds(5));
        long count = service.count(q -> q.wildcard("name", "*searchable*"));
        Page<SearchTestDocument> page = service.findPage(q -> q.wildcard("name", "*searchable*"), PageRequest.of(0, 1));

        // then
        assertEquals(2, count);
        assertEquals(2, page.getTotalElements());
        assertEquals(1, page.getContent().size());
    }

    @Test
    void shouldFilterByFuzzy() throws InterruptedException {
        // given
        service.createAll(List.of(
                document("code-1", "Searchable value"),
                document("code-2", "Different value")
        ));

        // when
        waitUntilCount(q -> q.fuzzy("name", "serchable"), 1, Duration.ofSeconds(5));
        long count = service.count(q -> q.fuzzy("name", "serchable"));

        // then
        assertEquals(1, count);
    }

    @Test
    void shouldFilterByQueryString() throws InterruptedException {
        // given
        service.createAll(List.of(
                document("alpha-1", "searchable title"),
                document("beta-1", "another title")
        ));

        // when
        waitUntilCount(q -> q.queryString("name:searchable"), 1, Duration.ofSeconds(5));
        long count = service.count(q -> q.queryString("name:searchable"));

        // then
        assertEquals(1, count);
    }

    @Test
    void shouldComposeAndOrAndNotPredicates() throws InterruptedException {
        // given
        service.createAll(List.of(
                document("alpha-code", "Searchable alpha"),
                document("beta-code", "Searchable beta"),
                document("gamma-code", "Not matched")
        ));

        // when
        waitUntilCount(q -> q.match("name", "searchable")
                .must(q.term("code", "alpha-code").should(q.term("code", "beta-code")))
                .must(q.term("code", "gamma-code").not()), 2, Duration.ofSeconds(5));

        List<SearchTestDocument> result = service.findAll(q -> q.match("name", "searchable")
                .must(q.term("code", "alpha-code").should(q.term("code", "beta-code")))
                .must(q.term("code", "gamma-code").not()));

        // then
        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(document -> "gamma-code".equals(document.getCode())));
    }
}
