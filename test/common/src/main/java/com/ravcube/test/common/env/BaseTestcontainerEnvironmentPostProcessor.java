package com.ravcube.test.common.env;

import com.ravcube.test.common.container.SharedContainer;
import java.util.Map;
import java.util.function.Predicate;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.testcontainers.lifecycle.Startable;

public abstract class BaseTestcontainerEnvironmentPostProcessor<C extends Startable>
        extends BaseEnvironmentPostProcessor {

    @Override
    public final void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!shouldStart(environment, application)) {
            return;
        }

        C container = sharedContainer().start(
                resolveImage(environment),
                this::createContainer,
                runningChecker(),
                shutdownHookName()
        );
        addFirstPropertySource(environment, propertySourceName(), properties(container, environment));
    }

    protected boolean shouldStart(ConfigurableEnvironment environment, SpringApplication application) {
        return isProfileActive(environment, application, profile())
                && isEnabled(environment, enabledProperty());
    }

    protected String resolveImage(ConfigurableEnvironment environment) {
        return environment.getProperty(imageProperty(), defaultImage());
    }

    protected abstract SharedContainer<C> sharedContainer();

    protected abstract String propertySourceName();

    protected abstract String profile();

    protected abstract String enabledProperty();

    protected abstract String imageProperty();

    protected abstract String defaultImage();

    protected abstract String shutdownHookName();

    protected abstract Predicate<C> runningChecker();

    protected abstract C createContainer(String imageName);

    protected abstract Map<String, Object> properties(C container, ConfigurableEnvironment environment);
}
