package com.ravcube.lib.logger.core;

import com.ravcube.lib.logger.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggerConfiguration {

    @Bean
    public LoggerFactory loggerFactory() {
        return new SpringLoggerFactory();
    }
}
