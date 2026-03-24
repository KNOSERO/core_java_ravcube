plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":lib:common"))
    api(project(":lib:event:api"))
    api(project(":lib:event:builder"))
    api(project(":lib:event:kafka"))
    api(libs.spring.context)
    api(libs.spring.tx)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.spring.test)
    runtimeOnly(libs.junit.launcher)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
