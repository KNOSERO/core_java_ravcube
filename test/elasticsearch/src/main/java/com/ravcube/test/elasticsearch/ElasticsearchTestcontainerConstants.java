package com.ravcube.test.elasticsearch;

import org.springframework.core.env.Profiles;

interface ElasticsearchTestcontainerConstants {

    String PROPERTY_SOURCE_NAME = "ravcubeTestElasticsearchContainer";
    String ELASTICSEARCH_PROFILE = "test-elasticsearch";
    String ELASTICSEARCH_ENABLED_PROPERTY = "ravcube.testcontainers.elasticsearch.enabled";
    String ELASTICSEARCH_IMAGE_PROPERTY = "ravcube.testcontainers.elasticsearch.image";
    String SPRING_ELASTICSEARCH_URIS_PROPERTY = "spring.elasticsearch.uris";
    String DEFAULT_ELASTICSEARCH_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch:8.14.3";
    String ELASTICSEARCH_SHUTDOWN_HOOK_NAME = "ravcube-test-elasticsearch-stop";
    Profiles ELASTICSEARCH_TEST_PROFILE = Profiles.of(ELASTICSEARCH_PROFILE);
}
