package com.ravcube.test.elasticsearch;

import com.ravcube.test.common.container.SharedContainer;
import com.ravcube.test.common.env.BaseTestcontainerEnvironmentPostProcessor;
import java.util.Map;
import org.springframework.core.env.ConfigurableEnvironment;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

public final class ElasticsearchTestEnvironmentPostProcessor
        extends BaseTestcontainerEnvironmentPostProcessor<ElasticsearchContainer>
        implements ElasticsearchTestcontainerConstants {

    private static final SharedContainer<ElasticsearchContainer> SHARED_ELASTICSEARCH_CONTAINER = new SharedContainer<>();

    @Override
    protected SharedContainer<ElasticsearchContainer> sharedContainer() {
        return SHARED_ELASTICSEARCH_CONTAINER;
    }

    @Override
    protected String propertySourceName() {
        return PROPERTY_SOURCE_NAME;
    }

    @Override
    protected String profile() {
        return ELASTICSEARCH_PROFILE;
    }

    @Override
    protected String enabledProperty() {
        return ELASTICSEARCH_ENABLED_PROPERTY;
    }

    @Override
    protected String imageProperty() {
        return ELASTICSEARCH_IMAGE_PROPERTY;
    }

    @Override
    protected String defaultImage() {
        return DEFAULT_ELASTICSEARCH_IMAGE;
    }

    @Override
    protected String shutdownHookName() {
        return ELASTICSEARCH_SHUTDOWN_HOOK_NAME;
    }

    @Override
    protected java.util.function.Predicate<ElasticsearchContainer> runningChecker() {
        return ElasticsearchContainer::isRunning;
    }

    @Override
    protected ElasticsearchContainer createContainer(String imageName) {
        return new ElasticsearchContainer(DockerImageName.parse(imageName))
                .withEnv("discovery.type", "single-node")
                .withEnv("xpack.security.enabled", "false");
    }

    @Override
    protected Map<String, Object> properties(ElasticsearchContainer container, ConfigurableEnvironment environment) {
        return Map.of(SPRING_ELASTICSEARCH_URIS_PROPERTY, "http://" + container.getHttpHostAddress());
    }
}
