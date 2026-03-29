plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "core_java_ravcube"
include("lib:common")
include("lib:cache:api")
include("lib:cache:core")
include("lib:search:api")
include("lib:search:core")
include("lib:security:api")
include("lib:security:core")
include("lib:eureka:api")
include("lib:eureka:core")
include("lib:data")
include("lib:event:api")
include("lib:event:core")
include("test:elasticsearch")
include("test:common")
include("test:eureka")
include("test:kafka")
include("test:keycloak")
include("test:postgresql")
include("test:redis")
