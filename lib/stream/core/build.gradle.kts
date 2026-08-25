plugins {
    `java-library`
}

dependencies {
    api(project(":lib:stream:common"))
    api(project(":lib:stream:api"))
    implementation(project(":lib:event:core"))
    implementation(libs.spring.context)
    implementation(libs.spring.web)

    testImplementation(libs.junit.jupiter)
    runtimeOnly(libs.junit.launcher)
}
