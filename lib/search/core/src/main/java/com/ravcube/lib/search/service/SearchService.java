package com.ravcube.lib.search.service;

import com.ravcube.lib.search.query.SearchPredicate;
import com.ravcube.lib.search.query.SearchQuery;
import com.ravcube.lib.search.repository.GenericSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;


public abstract class SearchService<T, ID> {

    private static final SearchQuery SEARCH_QUERY = new SearchQuery();
    protected GenericSearchRepository<T, ID> repository;

    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }

    public Optional<T> findOne(Function<SearchQuery, SearchPredicate> params) {
        return repository.findOne(toPredicate(params));
    }

    public List<T> findAll(Function<SearchQuery, SearchPredicate> params) {
        return repository.findAll(toPredicate(params));
    }

    public Page<T> findPage(Function<SearchQuery, SearchPredicate> params, Pageable pageable) {
        return repository.findPage(toPredicate(params), pageable);
    }

    public long count(Function<SearchQuery, SearchPredicate> params) {
        return repository.count(toPredicate(params));
    }

    public boolean exists(Function<SearchQuery, SearchPredicate> params) {
        return repository.exists(toPredicate(params));
    }

    public T create(T document) {
        return repository.save(document);
    }

    public Iterable<T> createAll(Iterable<T> documents) {
        return repository.saveAll(documents);
    }

    public void deleteById(ID id) {
        repository.deleteById(id);
    }

    public void delete(T document) {
        repository.delete(document);
    }

    @Autowired
    void setRepository(GenericSearchRepository<T, ID> repository) {
        this.repository = repository;
    }

    private SearchPredicate toPredicate(Function<SearchQuery, SearchPredicate> params) {
        Function<SearchQuery, SearchPredicate> queryParams = Objects.requireNonNull(params, "params must not be null");
        SearchPredicate predicate = queryParams.apply(SEARCH_QUERY);
        return Objects.requireNonNull(predicate, "params must return predicate");
    }
}
