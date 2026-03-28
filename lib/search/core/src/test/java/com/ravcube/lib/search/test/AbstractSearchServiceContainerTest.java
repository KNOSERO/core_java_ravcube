package com.ravcube.lib.search.test;

import com.ravcube.lib.search.doc.SearchTestDocument;
import com.ravcube.lib.search.doc.SearchTestRelation;
import com.ravcube.lib.search.doc.SearchTestRelationDetail;
import com.ravcube.lib.search.query.SearchPredicate;
import com.ravcube.lib.search.query.SearchQuery;
import com.ravcube.lib.search.repo.SearchTestRepository;
import com.ravcube.lib.search.service.SearchTestService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

@ActiveProfiles({"elasticsearch", "test-elasticsearch"})
@SpringBootTest(classes = TestApplication.class)
abstract class AbstractSearchServiceContainerTest {

    @Autowired
    protected SearchTestService service;

    @Autowired
    protected SearchTestRepository repository;

    @BeforeEach
    void clearIndex() throws InterruptedException {
        repository.deleteAll();
        waitUntil(() -> repository.count() == 0, Duration.ofSeconds(5));
    }

    protected SearchTestDocument document(String code, String name) {
        return document(code, name, 0);
    }

    protected SearchTestDocument document(String code, String name, int priority) {
        return document(code, name, priority, List.of());
    }

    protected SearchTestDocument document(String code, String name, int priority, List<SearchTestRelation> relations) {
        return document(code, name, priority, relations, List.of());
    }

    protected SearchTestDocument document(String code, String name, int priority,
                                          List<SearchTestRelation> relations, List<SearchTestRelation> labels) {
        return new SearchTestDocument(UUID.randomUUID().toString(), code, name, priority, relations, labels);
    }

    protected SearchTestRelation relation(String key, String value) {
        return new SearchTestRelation(key, value);
    }

    protected SearchTestRelation relation(String key, String value, List<SearchTestRelationDetail> details) {
        return new SearchTestRelation(key, value, details);
    }

    protected SearchTestRelationDetail relationDetail(String key, String value) {
        return new SearchTestRelationDetail(key, value);
    }

    protected void waitUntilCount(Function<SearchQuery, SearchPredicate> params, long expectedCount, Duration timeout)
            throws InterruptedException {
        waitUntil(() -> service.count(params) == expectedCount, timeout);
    }

    protected void waitUntil(BooleanSupplier condition, Duration timeout) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            Thread.sleep(100);
        }
    }
}
