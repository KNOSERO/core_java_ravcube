package com.ravcube.lib.search.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface GenericSearchRepository<T, ID> extends ElasticsearchRepository<T, ID>, SearchQueryExecutor<T> {
}
