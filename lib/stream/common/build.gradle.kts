plugins {
    `java-library`
}

dependencies {
    api(project(":lib:event:common"))

    testImplementation(libs.junit.jupiter)
    runtimeOnly(libs.junit.launcher)
}
