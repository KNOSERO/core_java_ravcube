package com.ravcube.lib.stream.config;

import com.ravcube.lib.event.publisher.ConfigRoutingEventPublisher;
import com.ravcube.lib.logger.core.LoggerConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@ComponentScan(basePackages = "com.ravcube.lib.stream")
@Import({
        ConfigRoutingEventPublisher.class,
        LoggerConfiguration.class
})
public class ClientStreamApiConfiguration {
}
