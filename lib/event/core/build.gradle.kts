plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

dependencies {
    implementation(project(":lib:common"))
    api(project(":lib:event:common"))
    api(project(":lib:logger:api"))
    implementation(libs.jackson.databind)
    api(libs.spring.context)
    api(libs.spring.kafka)
    api(libs.spring.tx)

    testImplementation(libs.junit.jupiter)
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    runtimeOnly(libs.junit.launcher)
}
