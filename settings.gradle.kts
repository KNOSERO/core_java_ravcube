plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "core_java_ravcube"
include("lib:common")
include("lib:cache:api")
include("lib:cache:core")
include("lib:data")
include("lib:event:api")
include("lib:event:core")
include("test:kafka")
include("test:postgresql")
include("test:redis")
