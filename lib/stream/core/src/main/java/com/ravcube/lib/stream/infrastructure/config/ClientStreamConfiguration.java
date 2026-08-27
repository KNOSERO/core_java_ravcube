package com.ravcube.lib.stream.infrastructure.config;

import com.ravcube.lib.stream.infrastructure.metrics.ClientStreamMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientStreamConfiguration {

    @Bean
    ClientStreamMetrics clientStreamMetrics(ObjectProvider<MeterRegistry> registries) {
        return ClientStreamMetrics.from(registries);
    }
}
