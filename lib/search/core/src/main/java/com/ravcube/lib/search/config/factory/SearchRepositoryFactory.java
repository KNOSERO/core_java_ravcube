package com.ravcube.lib.search.config.factory;

import com.ravcube.lib.search.repository.GenericSearchRepositoryImpl;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchRepositoryFactory;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;

class SearchRepositoryFactory extends ElasticsearchRepositoryFactory {

    private final ElasticsearchOperations operations;

    SearchRepositoryFactory(ElasticsearchOperations operations) {
        super(operations);
        this.operations = operations;
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        return GenericSearchRepositoryImpl.class;
    }

    @Override
    protected Object getTargetRepository(RepositoryInformation information) {
        return new GenericSearchRepositoryImpl<>(
                getEntityInformation(information.getDomainType()),
                operations
        );
    }
}
