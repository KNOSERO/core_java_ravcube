plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.spring.test)
    api(libs.testcontainers.elasticsearch)
    runtimeOnly(libs.junit.launcher)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
