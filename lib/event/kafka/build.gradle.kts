plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":lib:event:api"))
    api(libs.spring.kafka)

    testImplementation(libs.spring.test)
    testImplementation(libs.testcontainers.kafka)
    testImplementation(libs.testcontainers.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
