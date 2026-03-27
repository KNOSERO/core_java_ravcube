package com.ravcube.lib.cache.config;

import com.ravcube.lib.cache.CacheStore;
import com.ravcube.lib.cache.DefaultCacheStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!redis")
public class DefaultConfigCache {

    @Bean
    CacheStore defaultCacheStore() {
        return new DefaultCacheStore();
    }
}
