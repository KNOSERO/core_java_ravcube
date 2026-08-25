plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

dependencies {
    implementation(project(":lib:common"))
    api(project(":lib:event:api"))
    implementation(libs.jackson.databind)
    implementation(libs.nats.java)
    api(libs.spring.context)
    api(libs.spring.kafka)
    api(libs.spring.tx)

    testImplementation(libs.junit.jupiter)
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation(project(":test:common"))
    testImplementation(project(":test:kafka"))
    testImplementation(project(":test:nats"))
    testImplementation(libs.spring.test)
    runtimeOnly(libs.junit.launcher)
}
