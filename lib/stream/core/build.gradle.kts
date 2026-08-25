plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":lib:stream:common"))
    implementation(libs.spring.context)
    implementation(libs.spring.web)

    testImplementation(libs.junit.jupiter)
    runtimeOnly(libs.junit.launcher)
}

kotlin {
    jvmToolchain(21)
}
