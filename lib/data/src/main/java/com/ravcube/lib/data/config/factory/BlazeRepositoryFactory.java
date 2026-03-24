package com.ravcube.lib.data.config.factory;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;

class BlazeRepositoryFactory extends JpaRepositoryFactory {
    private final CriteriaBuilderFactory cbf;
    private final EntityViewManager evm;

    BlazeRepositoryFactory(EntityManager em, CriteriaBuilderFactory cbf, EntityViewManager evm) {
        super(em);
        this.cbf = cbf;
        this.evm = evm;
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        return EntityViewExecutorImpl.class;
    }

    @Override
    protected JpaRepositoryImplementation<?, ?> getTargetRepository(RepositoryInformation info, EntityManager em) {
        @SuppressWarnings({"rawtypes","unchecked"})
        JpaEntityInformation entityInfo = getEntityInformation(info.getDomainType());
        return new EntityViewExecutorImpl<>(entityInfo, em, cbf, evm);
    }
}
