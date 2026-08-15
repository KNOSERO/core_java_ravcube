plugins {
    `java-library`
}

dependencies {
    api(project(":lib:security:api"))
    implementation(platform(libs.spring.cloud.dependencies))
    implementation(libs.spring.boot.security)
    implementation(libs.spring.boot.oauth2.resource.server)
    implementation(libs.spring.web)
    compileOnly(libs.jakarta.servlet.api)

    testImplementation(libs.spring.test)
    testImplementation(libs.jakarta.servlet.api)
    testImplementation(project(":lib:eureka:core"))
    testImplementation(project(":test:awaitility"))
    testImplementation(project(":test:eureka"))
    testImplementation(project(":test:keycloak"))
    runtimeOnly(libs.junit.launcher)
}
