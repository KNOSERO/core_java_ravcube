package com.ravcube.lib.search.config.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;

public class SearchRepositoryFactoryBean<R extends Repository<T, I>, T, I extends Serializable>
        extends ElasticsearchRepositoryFactoryBean<R, T, I> {

    private ElasticsearchOperations operations;

    public SearchRepositoryFactoryBean(Class<? extends R> repositoryInterface) {
        super(repositoryInterface);
    }

    @Override
    @Autowired
    public void setElasticsearchOperations(ElasticsearchOperations operations) {
        this.operations = operations;
        super.setElasticsearchOperations(operations);
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory() {
        return new SearchRepositoryFactory(operations);
    }
}
