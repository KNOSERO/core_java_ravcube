package com.ravcube.lib.data.repository;

import com.querydsl.core.types.EntityPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface GenericRepository<T, ID> extends JpaRepository<T, ID>,
        QuerydslPredicateExecutor<T>, QuerydslBinderCustomizer<EntityPath<T>>,
        EntityViewExecutor<T> {

    @Override
    default void customize(QuerydslBindings b, EntityPath<T> root) {

    }

}
