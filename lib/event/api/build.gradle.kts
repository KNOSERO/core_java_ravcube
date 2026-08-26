plugins {
    `java-library`
}

dependencies {
    api(project(":lib:event:common"))
    implementation(project(":lib:event:core"))
    implementation(project(":lib:logger:core"))
    implementation(libs.spring.context)

    testImplementation(libs.junit.jupiter)
    testImplementation(project(":test:kafka"))
    testImplementation(libs.spring.test)
    testImplementation(libs.spring.tx)
    runtimeOnly(libs.junit.launcher)
}
