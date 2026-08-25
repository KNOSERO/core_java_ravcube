plugins {
    `java-library`
}

dependencies {
    api(project(":lib:stream:api"))
    implementation(libs.spring.context)
    implementation(libs.spring.tx)
    implementation(libs.spring.web)

    testImplementation(libs.junit.jupiter)
    runtimeOnly(libs.junit.launcher)
}
