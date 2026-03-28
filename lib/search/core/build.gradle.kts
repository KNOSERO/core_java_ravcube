plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":lib:search:api"))
    implementation(libs.spring.data.elasticsearch)

    testImplementation(project(":test:elasticsearch"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
