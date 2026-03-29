package com.ravcube.test.common.env;

import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.Profiles;

public abstract class BaseEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    protected final boolean isProfileActive(
            ConfigurableEnvironment environment,
            SpringApplication application,
            String profile
    ) {
        return environment.acceptsProfiles(Profiles.of(profile))
                || application.getAdditionalProfiles().contains(profile);
    }

    protected final boolean isEnabled(ConfigurableEnvironment environment, String enabledProperty) {
        return environment.getProperty(enabledProperty, Boolean.class, true);
    }

    protected final void addFirstPropertySource(
            ConfigurableEnvironment environment,
            String propertySourceName,
            Map<String, Object> properties
    ) {
        environment.getPropertySources().addFirst(new MapPropertySource(propertySourceName, properties));
    }
}
