package com.ravcube.lib.search.repository;

import com.ravcube.lib.search.query.SearchPredicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchEntityInformation;
import org.springframework.data.elasticsearch.repository.support.SimpleElasticsearchRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class GenericSearchRepositoryImpl<T, ID> extends SimpleElasticsearchRepository<T, ID>
        implements GenericSearchRepository<T, ID> {

    private final ElasticsearchOperations operations;
    private final Class<T> entityClass;

    public GenericSearchRepositoryImpl(ElasticsearchEntityInformation<T, ID> metadata,
                                       ElasticsearchOperations operations) {
        super(metadata, operations);
        this.operations = Objects.requireNonNull(operations, "operations must not be null");
        this.entityClass = Objects.requireNonNull(metadata, "metadata must not be null").getJavaType();
    }

    @Override
    public Optional<T> findOne(SearchPredicate predicate) {
        final SearchHits<T> searchHits = operations.search(buildQuery(predicate), entityClass);
        if (!searchHits.hasSearchHits()) {
            return Optional.empty();
        }
        return Optional.of(searchHits.getSearchHit(0).getContent());
    }

    @Override
    public List<T> findAll(SearchPredicate predicate) {
        return operations.search(buildQuery(predicate), entityClass)
                .getSearchHits()
                .stream()
                .map(SearchHit::getContent)
                .toList();
    }

    @Override
    public Page<T> findPage(SearchPredicate predicate, Pageable pageable) {
        final Pageable page = Objects.requireNonNull(pageable, "pageable must not be null");
        final SearchHits<T> searchHits = operations.search(buildQuery(predicate, page), entityClass);
        final List<T> content = searchHits.getSearchHits()
                .stream()
                .map(SearchHit::getContent)
                .toList();

        return new PageImpl<>(content, page, searchHits.getTotalHits());
    }

    @Override
    public long count(SearchPredicate predicate) {
        return operations.count(buildQuery(predicate), entityClass);
    }

    @Override
    public boolean exists(SearchPredicate predicate) {
        return count(predicate) > 0;
    }

    private NativeQuery buildQuery(SearchPredicate predicate) {
        return buildQuery(predicate, null);
    }

    private NativeQuery buildQuery(SearchPredicate predicate,
                                   Pageable pageable) {
        SearchPredicate queryPredicate = Objects.requireNonNull(predicate, "predicate must not be null");
        return NativeQuery.builder()
                .withQuery(queryPredicate.toQuery())
                .withPageable(pageable)
                .build();
    }
}
