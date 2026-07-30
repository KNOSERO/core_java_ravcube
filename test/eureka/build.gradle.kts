plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":test:common"))
    api(platform(libs.spring.cloud.dependencies))
    api(libs.spring.boot.autoconfigure)
    api(libs.spring.cloud.starter.loadbalancer)
    runtimeOnly(libs.junit.launcher)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
