package com.ravcube.test.common.env;

import com.ravcube.test.common.container.SharedContainer;
import java.util.Map;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.env.MockEnvironment;
import org.testcontainers.lifecycle.Startable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaseTestcontainerEnvironmentPostProcessorTest {

    @Test
    void doesNotStartContainerWhenProfileIsMissing() {
        TestPostProcessor postProcessor = new TestPostProcessor();
        MockEnvironment environment = new MockEnvironment();
        SpringApplication application = new SpringApplication();

        postProcessor.postProcessEnvironment(environment, application);

        assertFalse(postProcessor.containerStarted());
        assertFalse(environment.getPropertySources().contains("testContainer"));
    }

    @Test
    void doesNotStartContainerWhenDisabled() {
        TestPostProcessor postProcessor = new TestPostProcessor();
        MockEnvironment environment = new MockEnvironment()
                .withProperty("ravcube.testcontainers.test.enabled", "false");
        SpringApplication application = new SpringApplication();
        application.setAdditionalProfiles("test-container");

        postProcessor.postProcessEnvironment(environment, application);

        assertFalse(postProcessor.containerStarted());
        assertFalse(environment.getPropertySources().contains("testContainer"));
    }

    @Test
    void startsContainerWhenProfileIsActiveAndEnabledDefaultsToTrue() {
        TestPostProcessor postProcessor = new TestPostProcessor();
        MockEnvironment environment = new MockEnvironment();
        SpringApplication application = new SpringApplication();
        application.setAdditionalProfiles("test-container");

        postProcessor.postProcessEnvironment(environment, application);

        assertTrue(postProcessor.containerStarted());
        assertEquals("localhost", environment.getProperty("example.host"));
    }

    @Test
    void resolvesConfiguredImage() {
        TestPostProcessor postProcessor = new TestPostProcessor();
        MockEnvironment environment = new MockEnvironment()
                .withProperty("ravcube.testcontainers.test.image", "example/image:2");
        SpringApplication application = new SpringApplication();
        application.setAdditionalProfiles("test-container");

        postProcessor.postProcessEnvironment(environment, application);

        assertEquals("example/image:2", postProcessor.startedImageName());
    }

    @Test
    void registersPropertiesAsFirstPropertySource() {
        TestPostProcessor postProcessor = new TestPostProcessor();
        MockEnvironment environment = new MockEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("existing", Map.of("example.host", "old")));
        SpringApplication application = new SpringApplication();
        application.setAdditionalProfiles("test-container");

        postProcessor.postProcessEnvironment(environment, application);

        assertSame(environment.getPropertySources().get("testContainer"), environment.getPropertySources().iterator().next());
        assertEquals("localhost", environment.getProperty("example.host"));
    }

    private static final class TestPostProcessor
            extends BaseTestcontainerEnvironmentPostProcessor<FakeStartable> {

        private final SharedContainer<FakeStartable> sharedContainer = new SharedContainer<>();
        private FakeStartable startedContainer;

        @Override
        protected SharedContainer<FakeStartable> sharedContainer() {
            return sharedContainer;
        }

        @Override
        protected String propertySourceName() {
            return "testContainer";
        }

        @Override
        protected String profile() {
            return "test-container";
        }

        @Override
        protected String enabledProperty() {
            return "ravcube.testcontainers.test.enabled";
        }

        @Override
        protected String imageProperty() {
            return "ravcube.testcontainers.test.image";
        }

        @Override
        protected String defaultImage() {
            return "example/image:1";
        }

        @Override
        protected String shutdownHookName() {
            return "test-container-stop";
        }

        @Override
        protected Predicate<FakeStartable> runningChecker() {
            return FakeStartable::isRunning;
        }

        @Override
        protected FakeStartable createContainer(String imageName) {
            startedContainer = new FakeStartable(imageName);
            return startedContainer;
        }

        @Override
        protected Map<String, Object> properties(FakeStartable container, ConfigurableEnvironment environment) {
            return Map.of("example.host", "localhost");
        }

        private boolean containerStarted() {
            return startedContainer != null && startedContainer.isRunning();
        }

        private String startedImageName() {
            return startedContainer.imageName();
        }
    }

    private static final class FakeStartable implements Startable {

        private final String imageName;
        private boolean running;

        private FakeStartable(String imageName) {
            this.imageName = imageName;
        }

        @Override
        public void start() {
            running = true;
        }

        @Override
        public void stop() {
            running = false;
        }

        private boolean isRunning() {
            return running;
        }

        private String imageName() {
            return imageName;
        }
    }
}
