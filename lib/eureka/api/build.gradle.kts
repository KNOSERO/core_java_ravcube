plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(platform(libs.spring.cloud.dependencies))
    api(libs.spring.cloud.starter.openfeign)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
