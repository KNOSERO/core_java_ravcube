plugins {
    `java-library`
}

dependencies {
    api(project(":lib:search:api"))
    implementation(libs.spring.data.elasticsearch)

    testImplementation(project(":test:elasticsearch"))
}