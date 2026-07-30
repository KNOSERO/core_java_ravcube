package com.ravcube.test.kafka;

interface KafkaTestcontainerConstants {

    String PROPERTY_SOURCE_NAME = "ravcubeTestKafkaContainer";
    String KAFKA_PROFILE = "test-kafka";
    String BOOTSTRAP_SERVERS_PROPERTY = "spring.kafka.bootstrap-servers";
    String KAFKA_ENABLED_PROPERTY = "ravcube.testcontainers.kafka.enabled";
    String KAFKA_IMAGE_PROPERTY = "ravcube.testcontainers.kafka.image";
    String DEFAULT_KAFKA_IMAGE = "confluentinc/cp-kafka:7.7.0";
    String KAFKA_SHUTDOWN_HOOK_NAME = "ravcube-test-kafka-stop";
}
