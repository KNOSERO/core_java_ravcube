plugins {
    `java-library`
}

subprojects {
    repositories {
        mavenCentral()
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        outputs.upToDateWhen { false }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}