package com.ravcube.lib.search.repository;

import com.ravcube.lib.search.query.SearchPredicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface SearchQueryExecutor<T> {

    Optional<T> findOne(SearchPredicate predicate);

    List<T> findAll(SearchPredicate predicate);

    Page<T> findPage(SearchPredicate predicate, Pageable pageable);

    long count(SearchPredicate predicate);

    boolean exists(SearchPredicate predicate);
}
