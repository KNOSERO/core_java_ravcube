plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":test:common"))
    api(libs.nats.java)
    api(libs.testcontainers.core)
    runtimeOnly(libs.junit.launcher)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
