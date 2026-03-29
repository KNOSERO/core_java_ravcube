plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":test:common"))
    api(libs.testcontainers.postgresql)
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.junit.launcher)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
