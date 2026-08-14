package com.ravcube.lib.faulttolerance.support;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

@Configuration
class UnavailableServiceInstanceConfiguration {

    private static final String SERVICE_ID = "unavailable-service";

    @Bean
    ServiceInstanceListSupplier unavailableServiceInstanceSupplier(
            @Value("${ravcube.test.unavailable-service.port}") int port
    ) {
        return new SingleServiceInstanceSupplier(new DefaultServiceInstance(
                SERVICE_ID + ":" + port,
                SERVICE_ID,
                "localhost",
                port,
                false
        ));
    }

    private record SingleServiceInstanceSupplier(ServiceInstance instance) implements ServiceInstanceListSupplier {

        @Override
        public String getServiceId() {
            return instance.getServiceId();
        }

        @Override
        public Flux<List<ServiceInstance>> get() {
            return Flux.just(List.of(instance));
        }
    }
}
