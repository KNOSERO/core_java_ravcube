plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.spring.test)
    api(libs.spring.kafka)
    api(libs.testcontainers.kafka)
    runtimeOnly(libs.junit.launcher)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
