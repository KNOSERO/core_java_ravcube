package com.ravcube.lib.search.test;

import com.ravcube.lib.search.document.SearchTestDocument;
import com.ravcube.lib.search.query.SearchQuery;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchServiceCrudContainerTest extends AbstractSearchServiceContainerTest {

    @Test
    void shouldCreateAndFindDocumentById() throws InterruptedException {
        // given
        SearchTestDocument document = document("alpha-code", "Alpha Name");
        service.create(document);

        // when
        waitUntil(() -> service.findById(document.getId()).isPresent(), Duration.ofSeconds(5));
        Optional<SearchTestDocument> result = service.findById(document.getId());

        // then
        assertTrue(result.isPresent());
        assertEquals(document.getCode(), result.orElseThrow().getCode());
        assertEquals(document.getName(), result.orElseThrow().getName());
    }

    @Test
    void shouldCreateManyAndFindAllWithMatchAll() throws InterruptedException {
        // given
        service.createAll(List.of(
                document("code-1", "First"),
                document("code-2", "Second")
        ));

        // when
        waitUntilCount(SearchQuery::matchAll, 2, Duration.ofSeconds(5));
        List<SearchTestDocument> result = service.findAll(SearchQuery::matchAll);

        // then
        assertEquals(2, result.size());
    }

    @Test
    void shouldDeleteDocumentById() throws InterruptedException {
        // given
        SearchTestDocument document = document("to-delete-id", "Delete by id");
        service.create(document);
        waitUntil(() -> service.findById(document.getId()).isPresent(), Duration.ofSeconds(5));

        // when
        service.deleteById(document.getId());
        waitUntil(() -> service.findById(document.getId()).isEmpty(), Duration.ofSeconds(5));

        // then
        assertTrue(service.findById(document.getId()).isEmpty());
    }

    @Test
    void shouldDeleteDocumentByEntity() throws InterruptedException {
        // given
        SearchTestDocument document = document("to-delete-entity", "Delete by entity");
        service.create(document);
        waitUntil(() -> service.findById(document.getId()).isPresent(), Duration.ofSeconds(5));

        // when
        service.delete(document);
        waitUntil(() -> service.findById(document.getId()).isEmpty(), Duration.ofSeconds(5));

        // then
        assertTrue(service.findById(document.getId()).isEmpty());
    }
}
