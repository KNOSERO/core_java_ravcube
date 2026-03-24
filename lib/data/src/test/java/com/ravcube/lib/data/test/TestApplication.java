package com.ravcube.lib.data.test;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = "com.ravcube.lib.data")
@EntityScan(basePackages = "com.ravcube.lib.data.entity")
class TestApplication {
}
