plugins {
    `java-library`
}

dependencies {
    api(project(":lib:security:api"))
    implementation(libs.spring.boot.security)
    implementation(libs.spring.boot.oauth2.resource.server)
    compileOnly(libs.jakarta.servlet.api)

    testImplementation(libs.spring.test)
    testImplementation(libs.jakarta.servlet.api)
    testImplementation(libs.spring.web)
    testImplementation(project(":test:keycloak"))
    runtimeOnly(libs.junit.launcher)
}
