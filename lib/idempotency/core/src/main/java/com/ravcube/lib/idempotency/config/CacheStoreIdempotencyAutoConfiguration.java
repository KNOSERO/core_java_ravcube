package com.ravcube.lib.idempotency.config;

import com.ravcube.lib.cache.CacheStore;
import com.ravcube.lib.idempotency.CacheStoreIdempotencyStore;
import io.github.josipmusa.idempotency.core.IdempotencyStore;
import io.github.josipmusa.idempotency.springboot.IdempotencyAutoConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@AutoConfigureBefore(IdempotencyAutoConfiguration.class)
@ConditionalOnBean(CacheStore.class)
public class CacheStoreIdempotencyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(IdempotencyStore.class)
    IdempotencyStore cacheStoreIdempotencyStore(
            CacheStore cacheStore,
            @Value("${ravcube.idempotency.key-prefix:idempotency}") String keyPrefix
    ) {
        return new CacheStoreIdempotencyStore(cacheStore, keyPrefix);
    }
}
