package com.ravcube.lib.security.test.support;

import com.ravcube.lib.security.test.client.SecurityAuthTestClient;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackageClasses = SecurityAuthTestClient.class)
@SpringBootApplication(scanBasePackages = "com.ravcube.lib.security")
public class SecurityEurekaFeignTestApplication {
}
