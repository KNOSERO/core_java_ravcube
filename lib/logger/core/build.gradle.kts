plugins {
    \`java-library\`
}

dependencies {
    api(project(":lib:logger:api"))
    implementation(libs.spring.context)
    implementation(libs.spring.jcl)

    testImplementation(libs.junit.jupiter)
    runtimeOnly(libs.junit.launcher)
}
