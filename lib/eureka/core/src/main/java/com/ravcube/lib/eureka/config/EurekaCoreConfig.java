package com.ravcube.lib.eureka.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("eureka")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.ravcube.lib.eureka.client")
public class EurekaCoreConfig {
}
