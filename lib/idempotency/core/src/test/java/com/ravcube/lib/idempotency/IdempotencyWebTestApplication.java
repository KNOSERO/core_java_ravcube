package com.ravcube.lib.idempotency;

import com.ravcube.lib.cache.config.RedisConfigCache;
import com.ravcube.lib.eureka.EnableRavcubeEurekaClients;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableRavcubeEurekaClients(basePackageClasses = IdempotencyShotClient.class)
@SpringBootApplication(scanBasePackageClasses = {
        IdempotencyShotController.class,
        RedisConfigCache.class
})
class IdempotencyWebTestApplication {
}
