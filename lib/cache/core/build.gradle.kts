plugins {
    `java-library`
}

dependencies {
    api(project(":lib:cache:api"))
    implementation(libs.spring.data.redis)

    testImplementation(project(":test:awaitility"))
    testImplementation(project(":test:redis"))
}
