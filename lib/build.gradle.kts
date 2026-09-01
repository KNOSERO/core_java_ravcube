import org.gradle.api.GradleException
import org.gradle.api.artifacts.ProjectDependency

plugins {
    `java-library`
}

val isCiBuild = providers.environmentVariable("CI")
    .map { it.equals("true", ignoreCase = true) }
    .orElse(false)

val verifyLibraryStructure by tasks.registering {
    group = "verification"
    description = "Verifies dependency direction and boundaries of production library modules."
    notCompatibleWithConfigurationCache(
        "The task validates the complete configured project dependency graph."
    )

    doLast {
        val productionConfigurations = setOf(
            "api",
            "implementation",
            "compileOnlyApi",
            "compileOnly",
            "runtimeOnly"
        )
        val libraryProjects = subprojects.filter { it.path.startsWith(":lib:") }
        val libraryPaths = libraryProjects.mapTo(mutableSetOf()) { it.path }
        val dependenciesByProject = libraryProjects.associate { libraryProject ->
            libraryProject.path to productionConfigurations
                .flatMap { configurationName ->
                    libraryProject.configurations
                        .findByName(configurationName)
                        ?.dependencies
                        ?.withType(ProjectDependency::class.java)
                        ?.map(ProjectDependency::getPath)
                        .orEmpty()
                }
                .toSet()
        }
        val violations = mutableListOf<String>()

        val structuredFamilies = libraryPaths
            .mapNotNull { path ->
                val layer = path.substringAfterLast(":")
                path.substringBeforeLast(":").takeIf {
                    layer in setOf("api", "common", "core")
                }
            }
            .toSet()
            .filter { family ->
                setOf("api", "common", "core").all { layer ->
                    "$family:$layer" in libraryPaths
                }
            }

        structuredFamilies.forEach { family ->
            val api = "$family:api"
            val common = "$family:common"
            val core = "$family:core"

            if (common !in dependenciesByProject.getValue(api)) {
                violations += "$api must depend on $common"
            }
            if (core !in dependenciesByProject.getValue(api)) {
                violations += "$api must depend on $core"
            }
            if (common !in dependenciesByProject.getValue(core)) {
                violations += "$core must depend on $common"
            }
            if (api in dependenciesByProject.getValue(core)) {
                violations += "$core must not depend on $api"
            }
            if (api in dependenciesByProject.getValue(common)
                || core in dependenciesByProject.getValue(common)
            ) {
                violations += "$common must not depend on its api or core module"
            }
        }

        dependenciesByProject
            .filterKeys { it.endsWith(":common") }
            .forEach { (source, dependencies) ->
                dependencies
                    .filterNot { it == ":lib:common" || it.endsWith(":common") }
                    .forEach { target ->
                        violations += "$source may depend only on common library modules, not $target"
                    }
            }

        val forbiddenCommonDependencyGroups = setOf(
            "co.elastic",
            "io.micrometer",
            "jakarta.persistence",
            "jakarta.servlet",
            "org.apache.kafka",
            "org.elasticsearch",
            "org.hibernate",
            "org.springframework",
            "org.testcontainers"
        )

        libraryProjects
            .filter { it.path.endsWith(":common") }
            .forEach { commonProject ->
                productionConfigurations
                    .mapNotNull(commonProject.configurations::findByName)
                    .flatMap { it.dependencies }
                    .filterNot { it is ProjectDependency }
                    .filter { dependency ->
                        forbiddenCommonDependencyGroups.any { forbiddenGroup ->
                            dependency.group == forbiddenGroup
                                || dependency.group?.startsWith("$forbiddenGroup.") == true
                        }
                    }
                    .forEach { dependency ->
                        violations += "${commonProject.path} must not depend on " +
                            "technical library ${dependency.group}:${dependency.name}"
                    }
            }

        dependenciesByProject
            .filterKeys { it.endsWith(":core") }
            .forEach { (source, dependencies) ->
                val sourceFamily = source.substringBeforeLast(":")
                dependencies
                    .filter { it.endsWith(":core") }
                    .filterNot { it.substringBeforeLast(":") == sourceFamily }
                    .forEach { target ->
                        violations += "$source must not depend on implementation module $target"
                    }
            }

        val visiting = linkedSetOf<String>()
        val visited = mutableSetOf<String>()

        fun visit(path: String) {
            if (path in visiting) {
                val cycle = (visiting.dropWhile { it != path } + path).joinToString(" -> ")
                violations += "production library dependency cycle: $cycle"
                return
            }
            if (!visited.add(path)) {
                return
            }

            visiting += path
            dependenciesByProject.getValue(path)
                .filter { it in libraryPaths }
                .forEach(::visit)
            visiting -= path
        }

        libraryPaths.forEach(::visit)

        if (violations.isNotEmpty()) {
            throw GradleException(
                violations.distinct().sorted().joinToString(
                    separator = "\n - ",
                    prefix = "Invalid library module structure:\n - "
                )
            )
        }

        logger.lifecycle(
            "Verified ${libraryProjects.size} library modules and " +
                "${structuredFamilies.size} api/common/core families."
        )
    }
}

subprojects {
    repositories {
        mavenCentral()
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        outputs.upToDateWhen { false }
        testLogging {
            events("failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showCauses = true
            showStackTraces = true
            showStandardStreams = isCiBuild.get()
        }
    }

    pluginManager.withPlugin("java-library") {
        tasks.named("check") {
            dependsOn(project(":lib").tasks.named("verifyLibraryStructure"))
        }
    }
}

tasks.named("check") {
    dependsOn(verifyLibraryStructure)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
