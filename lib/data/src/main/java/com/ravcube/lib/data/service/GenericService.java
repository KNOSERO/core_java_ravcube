package com.ravcube.lib.data.service;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BeanPath;
import com.ravcube.lib.data.entity.EntityClass;
import com.ravcube.lib.data.repository.GenericRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.SimpleEntityPathResolver;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class GenericService<T extends EntityClass<ID>, Q extends EntityPath<T>, ID> {

    protected GenericRepository<T, ID> repository;
    private final EntityPathResolver resolver = SimpleEntityPathResolver.INSTANCE;
    @SuppressWarnings("unchecked")
    protected Class<T> domainClass() {
        return (Class<T>) ResolvableType.forClass(getClass())
                .as(GenericService.class)
                .getGeneric(0)
                .resolve();
    }
    @SuppressWarnings("unchecked")
    protected Q entityPath() {
        return (Q) resolver.createPath(domainClass());
    }

    public Optional<T> findOne(Function<Q, Predicate> params) {
        Q path = entityPath();
        return repository.findOne(params.apply(path));
    }

    public List<T> findAll(Function<Q, Predicate> params) {
        Q path = entityPath();
        return asList(repository.findAll(params.apply(path)));
    }

    public <R> List<R> findAll(Function<Q, Predicate> params, Class<R> clazz) {
        Q path = entityPath();
        return asList(repository.findAll(params.apply(path), clazz));
    }

    public Iterable<T> findAll(Function<Q, Predicate> params, Sort sort) {
        Q path = entityPath();
        return asList(repository.findAll(params.apply(path), sort));
    }

    public Iterable<T> findAll(Function<Q, Predicate> params, OrderSpecifier<?>... orders) {
        Q path = entityPath();
        return asList(repository.findAll(params.apply(path), orders));
    }

    public Page<T> findPage(Function<Q, Predicate> params, Pageable pageable) {
        Q path = entityPath();
        return repository.findAll(params.apply(path), pageable);
    }

    public long count(Function<Q, Predicate> params) {
        Q path = entityPath();
        return repository.count(params.apply(path));
    }

    public boolean exists(Function<Q, Predicate> params) {
        Q path = entityPath();
        return repository.exists(params.apply(path));
    }

    public T create(T obj) {
        return repository.save(obj);
    }

    public void delete(ID id) {
        repository.deleteById(id);
    }

    public void delete(T obj) {
        delete(obj.getId());
    }

    public <S extends BeanPath<? extends T>> S treat(BeanPath<? extends T> path, Class<S> clazz) {
        return com.querydsl.jpa.JPAExpressions.treat(path, clazz);
    }

    <R> List<R> asList(Iterable<R> it) {
        return java.util.stream.StreamSupport.stream(it.spliterator(), false).toList();
    }

    @Autowired
    void setRepository(GenericRepository<T, ID> repository) {
        this.repository = repository;
    }
}

