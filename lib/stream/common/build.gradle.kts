plugins {
    `java-library`
}

dependencies {
    api(project(":lib:event:api"))

    testImplementation(libs.junit.jupiter)
    runtimeOnly(libs.junit.launcher)
}
