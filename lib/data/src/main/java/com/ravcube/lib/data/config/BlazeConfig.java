package com.ravcube.lib.data.config;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.integration.view.spring.EnableEntityViews;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.ravcube.lib.data.config.factory.BlazeRepositoryFactoryBean;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = "${ravcube.data.base-packages:com.ravcube}",
        repositoryFactoryBeanClass = BlazeRepositoryFactoryBean.class
)
@EnableEntityViews(basePackages = "${ravcube.data.base-packages:com.ravcube}")
public class BlazeConfig {

    @PersistenceUnit
    private EntityManagerFactory emf;

    @Bean
    public CriteriaBuilderFactory criteriaBuilderFactory() {
        return Criteria.getDefault().createCriteriaBuilderFactory(emf);
    }

    @Bean
    public EntityViewManager entityViewManager(CriteriaBuilderFactory cbf,
                                               EntityViewConfiguration evc) {
        return evc.createEntityViewManager(cbf);
    }
}
