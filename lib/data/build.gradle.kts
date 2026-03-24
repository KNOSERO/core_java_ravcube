plugins {
    `java-library`
    alias(libs.plugins.spring.dependency)
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.blaze.api)
    api(libs.blaze.impl)

    api(libs.blaze.spring)
    annotationProcessor(libs.blaze.processor)

    api(libs.blaze.querydsl)
    api(libs.blaze.hibernate)

    api(libs.querydsl.core)
    api(variantOf(libs.querydsl.jpa) { classifier("jakarta") })
    api(variantOf(libs.querydsl.apt) { classifier("jakarta") })
    annotationProcessor(variantOf(libs.querydsl.apt) { classifier("jakarta") })
    testAnnotationProcessor(variantOf(libs.querydsl.apt) { classifier("jakarta") })

    compileOnlyApi(libs.jakarta.persistence.api)
    compileOnlyApi(libs.jakarta.annotation.api)
    annotationProcessor(libs.jakarta.persistence.api)
    annotationProcessor(libs.jakarta.annotation.api)
    testAnnotationProcessor(libs.jakarta.persistence.api)
    testAnnotationProcessor(libs.jakarta.annotation.api)

    api(libs.spring.data.jpa)
    testImplementation(project(":test:postgresql"))

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
