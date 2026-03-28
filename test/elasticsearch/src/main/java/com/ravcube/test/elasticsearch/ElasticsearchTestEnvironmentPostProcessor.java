package com.ravcube.test.elasticsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public final class ElasticsearchTestEnvironmentPostProcessor
        implements EnvironmentPostProcessor, Ordered, ElasticsearchTestcontainerConstants {

    private static final SharedElasticsearchContainer SHARED_ELASTICSEARCH_CONTAINER = new SharedElasticsearchContainer();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!shouldStartElasticsearchContainer(environment, application)) {
            return;
        }

        ElasticsearchContainer elasticsearchContainer = SHARED_ELASTICSEARCH_CONTAINER.start(resolveElasticsearchImage(environment));
        registerElasticsearchProperties(environment, elasticsearchContainer);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private static final class SharedElasticsearchContainer {
        private ElasticsearchContainer elasticsearchContainer;

        synchronized ElasticsearchContainer start(String imageName) {
            if (elasticsearchContainer == null || !elasticsearchContainer.isRunning()) {
                ElasticsearchContainer container = new ElasticsearchContainer(DockerImageName.parse(imageName))
                        .withEnv("discovery.type", "single-node")
                        .withEnv("xpack.security.enabled", "false");
                container.start();
                Runtime.getRuntime().addShutdownHook(new Thread(container::stop, ELASTICSEARCH_SHUTDOWN_HOOK_NAME));
                elasticsearchContainer = container;
            }

            return elasticsearchContainer;
        }
    }

    private boolean shouldStartElasticsearchContainer(ConfigurableEnvironment environment, SpringApplication application) {
        return isElasticsearchProfileActive(environment, application) && isElasticsearchEnabled(environment);
    }

    private boolean isElasticsearchProfileActive(ConfigurableEnvironment environment, SpringApplication application) {
        return environment.acceptsProfiles(ELASTICSEARCH_TEST_PROFILE)
                || application.getAdditionalProfiles().contains(ELASTICSEARCH_PROFILE);
    }

    private boolean isElasticsearchEnabled(ConfigurableEnvironment environment) {
        return environment.getProperty(ELASTICSEARCH_ENABLED_PROPERTY, Boolean.class, true);
    }

    private String resolveElasticsearchImage(ConfigurableEnvironment environment) {
        return environment.getProperty(ELASTICSEARCH_IMAGE_PROPERTY, DEFAULT_ELASTICSEARCH_IMAGE);
    }

    private void registerElasticsearchProperties(ConfigurableEnvironment environment,
                                                 ElasticsearchContainer elasticsearchContainer) {
        Map<String, Object> elasticsearchProperties = Map.of(
                SPRING_ELASTICSEARCH_URIS_PROPERTY, "http://" + elasticsearchContainer.getHttpHostAddress()
        );

        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, elasticsearchProperties));
    }
}
