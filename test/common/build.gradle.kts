plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.spring.test)
    api(libs.testcontainers.junit.jupiter)
    testImplementation(libs.junit.jupiter)
    runtimeOnly(libs.junit.launcher)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
