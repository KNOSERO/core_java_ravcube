package com.ravcube.test.nats;

interface NatsTestcontainerConstants {

    String PROPERTY_SOURCE_NAME = "ravcubeTestNatsContainer";
    String NATS_PROFILE = "test-nats";
    String NATS_URL_PROPERTY = "ravcube.nats.url";
    String NATS_ENABLED_PROPERTY = "ravcube.testcontainers.nats.enabled";
    String NATS_IMAGE_PROPERTY = "ravcube.testcontainers.nats.image";
    String DEFAULT_NATS_IMAGE = "nats:2.12-alpine";
    String NATS_SHUTDOWN_HOOK_NAME = "ravcube-test-nats-stop";
}
