package com.ravcube.lib.data.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface EntityViewExecutor<T> {
    <V> Optional<V> findOne(Predicate predicate, Class<V> viewType);

    <V> Iterable<V> findAll(Predicate predicate, Class<V> viewType);

    <V> Iterable<V> findAll(Predicate predicate, Class<V> viewType, Sort sort);

    <V> Iterable<V> findAll(Predicate predicate, Class<V> viewType, OrderSpecifier<?>... orders);

    <V> Iterable<V> findAll(Class<V> viewType, OrderSpecifier<?>... orders);

    <V> Page<V> findAll(Predicate predicate, Class<V> viewType, Pageable pageable);
}

