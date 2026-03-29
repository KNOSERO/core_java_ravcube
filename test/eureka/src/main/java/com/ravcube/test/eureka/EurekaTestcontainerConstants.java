package com.ravcube.test.eureka;

interface EurekaTestcontainerConstants {

    String PROPERTY_SOURCE_NAME = "ravcubeTestEurekaContainer";
    String EUREKA_PROFILE = "test-eureka";
    String EUREKA_ENABLED_PROPERTY = "ravcube.testcontainers.eureka.enabled";
    String EUREKA_IMAGE_PROPERTY = "ravcube.testcontainers.eureka.image";
    String EUREKA_COUNT_PROPERTY = "ravcube.testcontainers.eureka.count";
    String EUREKA_SERVICE_URL_PROPERTY = "eureka.client.service-url.defaultZone";
    String DEFAULT_EUREKA_SERVICE_URL = "http://localhost:8761/eureka/";
    int DEFAULT_EUREKA_SERVER_COUNT = 1;
    int EUREKA_INTERNAL_PORT = 8761;
    String DEFAULT_EUREKA_IMAGE = "springcloud/eureka:latest";
    String EUREKA_SHUTDOWN_HOOK_NAME_PREFIX = "ravcube-test-eureka-stop-";
}
