package com.ravcube.lib.data.config.factory;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLSerializer;
import com.querydsl.jpa.JPQLTemplates;
import com.ravcube.lib.data.repository.EntityViewExecutor;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@NoRepositoryBean
@Transactional(readOnly = true)
class EntityViewExecutorImpl<T, ID extends Serializable>
        extends SimpleJpaRepository<T, ID> implements EntityViewExecutor<T> {
    private final EntityPath<T> path;
    private final PathBuilder<T> builder;
    private final EntityManager entityManager;
    private CriteriaBuilderFactory criteriaBuilderFactory;
    private EntityViewManager entityViewManager;

    EntityViewExecutorImpl(JpaEntityInformation<T, ID> info,
                                  EntityManager entityManager,
                                  CriteriaBuilderFactory criteriaBuilderFactory,
                                  EntityViewManager entityViewManager) {
        super(info, entityManager);
        this.entityManager = entityManager;
        this.criteriaBuilderFactory = criteriaBuilderFactory;
        this.entityViewManager = entityViewManager;
        this.path = SimpleEntityPathResolver.INSTANCE.createPath(info.getJavaType());
        this.builder = new PathBuilder<>(path.getType(), path.getMetadata());
    }

    @Override
    public <V> Optional<V> findOne(Predicate predicate, Class<V> viewType) {
        final Class<? extends T> entityClass = path.getType();
        final String alias = BlazePathBuilder.aliasOf(builder);
        return QueryBlaze.findOne(entityManager, criteriaBuilderFactory, entityViewManager,
                entityClass, alias, predicate, viewType);
    }

    @Override
    public <V> Iterable<V> findAll(Predicate predicate, Class<V> viewType) {
        final Class<? extends T> entityClass = path.getType();
        final String alias = BlazePathBuilder.aliasOf(builder);
        return QueryBlaze.findAll(entityManager, criteriaBuilderFactory, entityViewManager,
                entityClass, alias, predicate, viewType);
    }

    @Override
    public <V> Iterable<V> findAll(Predicate predicate, Class<V> viewType, Sort sort) {
        final Class<? extends T> entityClass = path.getType();
        final String alias = BlazePathBuilder.aliasOf(builder);
        return QueryBlaze.findAll(entityManager, criteriaBuilderFactory, entityViewManager,
                entityClass, alias, predicate, viewType, sort);
    }

    @Override
    public <V> Iterable<V> findAll(Predicate predicate, Class<V> viewType, OrderSpecifier<?>... orders) {
        final Class<? extends T> entityClass = path.getType();
        final String alias = BlazePathBuilder.aliasOf(builder);
        return QueryBlaze.findAll(entityManager, criteriaBuilderFactory, entityViewManager,
                entityClass, alias, predicate, viewType, orders);
    }

    @Override
    public <V> Iterable<V> findAll(Class<V> viewType, OrderSpecifier<?>... orders) {
        final Class<? extends T> entityClass = path.getType();
        final String alias = BlazePathBuilder.aliasOf(builder);
        return QueryBlaze.findAll(entityManager, criteriaBuilderFactory, entityViewManager,
                entityClass, alias, viewType, orders);
    }

    @Override
    public <V> Page<V> findAll(Predicate predicate, Class<V> viewType, Pageable pageable) {
        final Class<? extends T> entityClass = path.getType();
        final String alias = BlazePathBuilder.aliasOf(builder);
        return QueryBlaze.findAll(entityManager, criteriaBuilderFactory, entityViewManager,
                entityClass, alias, predicate, viewType, pageable);
    }

    static class QueryBlaze {
        public static <T, V> Optional<V> findOne(
                EntityManager em, CriteriaBuilderFactory cbf, EntityViewManager evm,
                Class<T> entityClass, String alias,
                Predicate predicate, Class<V> viewType
        ) {
            var cb = BlazeUtils.baseCb(em, cbf, entityClass, alias);
            BlazeUtils.applyPredicate(cb, predicate);
            cb.setMaxResults(1);
            var setting = EntityViewSetting.create(viewType);
            var list = evm.applySetting(setting, cb).getResultList();
            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        }

        public static <T, V> List<V> findAll(
                EntityManager em, CriteriaBuilderFactory cbf, EntityViewManager evm,
                Class<T> entityClass, String alias,
                Predicate predicate, Class<V> viewType
        ) {
            var cb = BlazeUtils.baseCb(em, cbf, entityClass, alias);
            BlazeUtils.applyPredicate(cb, predicate);
            var setting = EntityViewSetting.create(viewType);
            return evm.applySetting(setting, cb).getResultList();
        }

        public static <T, V> List<V> findAll(
                EntityManager em, CriteriaBuilderFactory cbf, EntityViewManager evm,
                Class<T> entityClass, String alias,
                Predicate predicate, Class<V> viewType, Sort sort
        ) {
            var cb = BlazeUtils.baseCb(em, cbf, entityClass, alias);
            BlazeUtils.applyPredicate(cb, predicate);
            BlazeUtils.applySpringSort(cb, alias, sort);
            var setting = EntityViewSetting.create(viewType);
            return evm.applySetting(setting, cb).getResultList();
        }

        public static <T, V> List<V> findAll(
                EntityManager em, CriteriaBuilderFactory cbf, EntityViewManager evm,
                Class<T> entityClass, String alias,
                Class<V> viewType, OrderSpecifier<?>... orders
        ) {
            var cb = BlazeUtils.baseCb(em, cbf, entityClass, alias);
            BlazeUtils.applyOrderSpecifiers(cb, alias, orders);
            var setting = EntityViewSetting.create(viewType);
            return evm.applySetting(setting, cb).getResultList();
        }

        public static <T, V> List<V> findAll(
                EntityManager em, CriteriaBuilderFactory cbf, EntityViewManager evm,
                Class<T> entityClass, String alias,
                Predicate predicate, Class<V> viewType, OrderSpecifier<?>... orders
        ) {
            var cb = BlazeUtils.baseCb(em, cbf, entityClass, alias);
            BlazeUtils.applyPredicate(cb, predicate);
            BlazeUtils.applyOrderSpecifiers(cb, alias, orders);
            var setting = EntityViewSetting.create(viewType);
            return evm.applySetting(setting, cb).getResultList();
        }

        public static <T, V> Page<V> findAll(
                EntityManager em, CriteriaBuilderFactory cbf, EntityViewManager evm,
                Class<T> entityClass, String alias,
                Predicate predicate, Class<V> viewType, Pageable pageable
        ) {
            var cb = BlazeUtils.baseCb(em, cbf, entityClass, alias);
            BlazeUtils.applyPredicate(cb, predicate);
            if (pageable != null) {
                BlazeUtils.applySpringSort(cb, alias, pageable.getSort());
                if (pageable.isPaged()) {
                    cb.setFirstResult((int) pageable.getOffset());
                    cb.setMaxResults(pageable.getPageSize());
                }
            }
            var setting = EntityViewSetting.create(viewType);
            var content = evm.applySetting(setting, cb).getResultList();

            long total = -1;
            if (pageable == null || pageable.isPaged()) {
                var countCb = BlazeUtils.baseCb(em, cbf, entityClass, alias);
                BlazeUtils.applyPredicate(countCb, predicate);
                total = BlazeUtils.count(countCb, alias);
            }
            return new PageImpl<>(content, pageable == null ? PageRequest.of(0, content.size()) : pageable,
                    total < 0 ? content.size() : total);
        }

        static class BlazeUtils {

            public static <T> CriteriaBuilder<T> baseCb(
                    EntityManager em, CriteriaBuilderFactory cbf,
                    Class<T> entityClass, String alias
            ) {
                if (alias == null || alias.isBlank()) alias = "e";
                return cbf.create(em, entityClass).from(entityClass, alias);
            }

            public static void applyPredicate(CriteriaBuilder<?> cb, Predicate predicate) {
                if (predicate == null) return;
                JPQLSerializer ser = new JPQLSerializer(JPQLTemplates.DEFAULT);
                ser.handle(predicate);
                cb.whereExpression(ser.toString());
                var params = ser.getConstants();
                for (int i = 0; i < params.size(); i++) {
                    cb.setParameter(String.valueOf(i + 1), params.get(i));
                }
            }

            public static void applySpringSort(CriteriaBuilder<?> cb, String alias, Sort sort) {
                if (sort == null || sort.isUnsorted()) return;
                for (Sort.Order o : sort) {
                    String prop = alias + "." + o.getProperty();
                    if (o.isAscending()) cb.orderByAsc(prop); else cb.orderByDesc(prop);
                }
            }

            public static void applyOrderSpecifiers(CriteriaBuilder<?> cb, String alias, OrderSpecifier<?>... orders) {
                if (orders == null) return;
                for (OrderSpecifier<?> os : orders) {
                    String target = os.getTarget().toString();
                    if (!target.contains(".")) target = alias + "." + target;
                    if (os.isAscending()) cb.orderByAsc(target); else cb.orderByDesc(target);
                }
            }

            public static long count(CriteriaBuilder<?> cb, String alias) {
                var count = cb.copy(Long.class).select("COUNT(" + alias + ")");
                return ((Number) count.getSingleResult()).longValue();
            }
        }
    }

    static class BlazePathBuilder {
        public static String aliasOf(PathBuilder<?> builder) {
            if (builder != null && builder.getMetadata() != null && builder.getMetadata().getName() != null) {
                return builder.getMetadata().getName();
            }
            return "e";
        }
    }
}
