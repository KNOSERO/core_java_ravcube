plugins {
    `java-library`
}

dependencies {
    api(project(":lib:stream:common"))
    implementation(project(":lib:stream:core"))
    implementation(project(":lib:event:core"))
    implementation(libs.spring.context)
    implementation(libs.spring.web)

    testImplementation(libs.spring.test)
    testImplementation(libs.junit.jupiter)
    runtimeOnly(libs.junit.launcher)
}
