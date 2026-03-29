plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":test:common"))
    runtimeOnly(libs.junit.launcher)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
