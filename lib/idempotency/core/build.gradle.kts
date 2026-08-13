plugins {
    `java-library`
}

dependencies {
    api(libs.idempotency.spring.boot.starter)
    implementation(project(":lib:cache:api"))
    implementation(libs.idempotency.inmemory)
    implementation(libs.spring.web)

    testImplementation(project(":lib:cache:core"))
    testImplementation(project(":lib:eureka:core"))
    testImplementation(project(":test:eureka"))
    testImplementation(project(":test:redis"))
    testImplementation(platform(libs.spring.cloud.dependencies))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.spring.cloud.starter.loadbalancer)
    testImplementation(libs.spring.cloud.starter.openfeign)
    testImplementation(libs.spring.test)
    runtimeOnly(libs.junit.launcher)
}
