plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

dependencies {
    implementation(project(":lib:common"))
    implementation(project(":lib:event:common"))
    implementation(project(":lib:logger:api"))
    implementation(libs.jackson.databind)
    implementation(libs.spring.context)
    implementation(libs.spring.kafka)
    implementation(libs.spring.tx)

    testImplementation(libs.junit.jupiter)
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    runtimeOnly(libs.junit.launcher)
}
