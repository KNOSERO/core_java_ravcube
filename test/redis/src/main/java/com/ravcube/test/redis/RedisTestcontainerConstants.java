package com.ravcube.test.redis;

import org.springframework.core.env.Profiles;

interface RedisTestcontainerConstants {

    String PROPERTY_SOURCE_NAME = "ravcubeTestRedisContainer";
    String REDIS_PROFILE = "test-redis";
    String REDIS_ENABLED_PROPERTY = "ravcube.testcontainers.redis.enabled";
    String REDIS_IMAGE_PROPERTY = "ravcube.testcontainers.redis.image";
    String REDIS_HOST_PROPERTY = "spring.data.redis.host";
    String REDIS_PORT_PROPERTY = "spring.data.redis.port";
    int REDIS_INTERNAL_PORT = 6379;
    String DEFAULT_REDIS_IMAGE = "redis:7.2-alpine";
    String REDIS_SHUTDOWN_HOOK_NAME = "ravcube-test-redis-stop";
    Profiles REDIS_TEST_PROFILE = Profiles.of(REDIS_PROFILE);
}
