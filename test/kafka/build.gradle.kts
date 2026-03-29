plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":test:common"))
    api(libs.spring.kafka)
    api(libs.testcontainers.kafka)
    runtimeOnly(libs.junit.launcher)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
