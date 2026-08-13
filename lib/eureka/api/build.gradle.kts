plugins {
    `java-library`
}

dependencies {
    api(platform(libs.spring.cloud.dependencies))
    api(libs.spring.cloud.starter.openfeign)
}
