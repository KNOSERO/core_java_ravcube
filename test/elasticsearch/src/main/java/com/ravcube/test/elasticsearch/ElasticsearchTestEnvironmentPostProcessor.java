package com.ravcube.test.elasticsearch;

import com.ravcube.test.common.container.SharedContainer;
import com.ravcube.test.common.env.BaseEnvironmentPostProcessor;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

public final class ElasticsearchTestEnvironmentPostProcessor
        extends BaseEnvironmentPostProcessor implements ElasticsearchTestcontainerConstants {

    private static final SharedContainer<ElasticsearchContainer> SHARED_ELASTICSEARCH_CONTAINER = new SharedContainer<>();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!isProfileActive(environment, application, ELASTICSEARCH_PROFILE)
                || !isEnabled(environment, ELASTICSEARCH_ENABLED_PROPERTY)) {
            return;
        }

        ElasticsearchContainer elasticsearchContainer = SHARED_ELASTICSEARCH_CONTAINER.start(
                resolveElasticsearchImage(environment),
                this::createElasticsearchContainer,
                ElasticsearchContainer::isRunning,
                ELASTICSEARCH_SHUTDOWN_HOOK_NAME
        );
        registerElasticsearchProperties(environment, elasticsearchContainer);
    }

    private String resolveElasticsearchImage(ConfigurableEnvironment environment) {
        return environment.getProperty(ELASTICSEARCH_IMAGE_PROPERTY, DEFAULT_ELASTICSEARCH_IMAGE);
    }

    private ElasticsearchContainer createElasticsearchContainer(String imageName) {
        return new ElasticsearchContainer(DockerImageName.parse(imageName))
                .withEnv("discovery.type", "single-node")
                .withEnv("xpack.security.enabled", "false");
    }

    private void registerElasticsearchProperties(ConfigurableEnvironment environment,
                                                 ElasticsearchContainer elasticsearchContainer) {
        Map<String, Object> elasticsearchProperties = Map.of(
                SPRING_ELASTICSEARCH_URIS_PROPERTY, "http://" + elasticsearchContainer.getHttpHostAddress()
        );

        addFirstPropertySource(environment, PROPERTY_SOURCE_NAME, elasticsearchProperties);
    }
}
