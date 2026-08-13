package com.ravcube.test.eureka;

import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import reactor.core.publisher.Flux;

@AutoConfiguration
@Profile(EurekaTestProfiles.TEST_EUREKA_PROFILE)
public class EurekaClientTestConfiguration {

    @Bean
    @Primary
    public ServiceInstanceListSupplier serviceInstanceListSupplier(Environment environment) {
        return new LocalTestServiceInstanceListSupplier(
                requiredProperty(environment, "spring.application.name"),
                environment.getProperty("eureka.instance.instance-id"),
                environment.getProperty("eureka.instance.ip-address"),
                environment.getProperty("eureka.instance.hostname", "localhost"),
                requiredProperty(environment, "eureka.instance.non-secure-port", Integer.class)
        );
    }

    private static final class LocalTestServiceInstanceListSupplier implements ServiceInstanceListSupplier {

        private final String serviceId;
        private final List<ServiceInstance> instances;

        private LocalTestServiceInstanceListSupplier(
                String serviceId,
                String instanceId,
                String ipAddress,
                String hostname,
                int port
        ) {
            this.serviceId = serviceId;
            this.instances = List.of(new DefaultServiceInstance(
                    normalizeInstanceId(serviceId, instanceId, ipAddress, hostname, port),
                    serviceId,
                    normalizeHost(ipAddress, hostname),
                    port,
                    false
            ));
        }

        @Override
        public String getServiceId() {
            return serviceId;
        }

        @Override
        public Flux<List<ServiceInstance>> get() {
            return Flux.just(instances);
        }

        private static String normalizeInstanceId(
                String serviceId,
                String instanceId,
                String ipAddress,
                String hostname,
                int port
        ) {
            if (instanceId != null && !instanceId.isBlank()) {
                return instanceId;
            }
            return serviceId + ":" + normalizeHost(ipAddress, hostname) + ":" + port;
        }

        private static String normalizeHost(String ipAddress, String hostname) {
            if (ipAddress != null && !ipAddress.isBlank()) {
                return ipAddress;
            }
            return hostname;
        }
    }

    private static String requiredProperty(Environment environment, String name) {
        return requiredProperty(environment, name, String.class);
    }

    private static <T> T requiredProperty(Environment environment, String name, Class<T> type) {
        T value = environment.getProperty(name, type);
        if (value == null) {
            throw new IllegalStateException("Missing required property: " + name);
        }
        return value;
    }
}
