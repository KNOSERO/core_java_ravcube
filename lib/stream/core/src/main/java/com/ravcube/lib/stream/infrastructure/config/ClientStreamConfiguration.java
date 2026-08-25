package com.ravcube.lib.stream.infrastructure.config;

import com.ravcube.lib.logger.core.LoggerConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(LoggerConfiguration.class)
public class ClientStreamConfiguration {
}
