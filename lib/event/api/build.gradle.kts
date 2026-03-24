plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.spring.context)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
