plugins {
    `java-library`
}

dependencies {
    api(project(":lib:eureka:api"))
    implementation(platform(libs.spring.cloud.dependencies))
    implementation(libs.spring.cloud.starter.eureka.client)
    implementation(libs.jackson.databind)

    testImplementation(libs.spring.test)
    testImplementation(libs.spring.web)
    testImplementation(libs.spring.cloud.starter.openfeign)
    testImplementation(project(":test:eureka"))
    runtimeOnly(libs.junit.launcher)
}
