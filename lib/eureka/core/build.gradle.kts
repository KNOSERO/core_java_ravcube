plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":lib:eureka:api"))
    implementation(platform(libs.spring.cloud.dependencies))
    implementation(libs.spring.cloud.starter.eureka.client)
    implementation(libs.jackson.databind)

    testImplementation(libs.spring.test)
    testImplementation(project(":test:eureka"))
    runtimeOnly(libs.junit.launcher)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
