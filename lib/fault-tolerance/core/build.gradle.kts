plugins {
    `java-library`
}

dependencies {
    api(platform(libs.spring.cloud.dependencies))
    api(libs.spring.cloud.starter.circuitbreaker.resilience4j)

    testImplementation(project(":lib:eureka:api"))
    testImplementation(project(":test:awaitility"))
    testImplementation(project(":test:common"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.spring.cloud.starter.loadbalancer)
    testImplementation(libs.spring.test)
    testImplementation(libs.spring.web)
    testRuntimeOnly(libs.junit.launcher)
}
