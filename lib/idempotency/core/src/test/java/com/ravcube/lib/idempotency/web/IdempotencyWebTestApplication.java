package com.ravcube.lib.idempotency.web;

import com.ravcube.lib.cache.config.RedisConfigCache;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackageClasses = IdempotencyShotClient.class)
@SpringBootApplication(scanBasePackageClasses = {
        IdempotencyShotController.class,
        RedisConfigCache.class
})
class IdempotencyWebTestApplication {
}
