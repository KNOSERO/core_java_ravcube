package com.ravcube.lib.search.config;

import com.ravcube.lib.search.config.factory.SearchRepositoryFactoryBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@Profile("elasticsearch")
@EnableElasticsearchRepositories(
        basePackages = "${ravcube.search.base-packages:com.ravcube}",
        repositoryFactoryBeanClass = SearchRepositoryFactoryBean.class
)
public class ElasticsearchRepositoryConfig {
}
