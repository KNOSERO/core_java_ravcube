plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":lib:cache:api"))
    implementation(libs.spring.data.redis)

    testImplementation(project(":test:redis"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
