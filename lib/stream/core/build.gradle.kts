plugins {
    `java-library`
}

dependencies {
    implementation(project(":lib:stream:common"))
    implementation(libs.spring.context)
    implementation(libs.spring.web)

    testImplementation(libs.junit.jupiter)
    runtimeOnly(libs.junit.launcher)
}
