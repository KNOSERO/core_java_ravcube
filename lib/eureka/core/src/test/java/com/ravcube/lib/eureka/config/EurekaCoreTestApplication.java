package com.ravcube.lib.eureka.config;

import com.ravcube.lib.eureka.client.TestClient;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackageClasses = TestClient.class)
@SpringBootApplication(scanBasePackages = "com.ravcube.lib.eureka")
public class EurekaCoreTestApplication {
}
