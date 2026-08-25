plugins {
    `java-library`
}

dependencies {
    api(project(":lib:stream:common"))
    implementation(project(":lib:stream:core"))
}
