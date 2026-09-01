val docsPort = providers.gradleProperty("docsPort").orElse("3000")
val docsContainerCommand = providers.gradleProperty("containerCommand").orElse("")
val docsPowerShellScript = layout.projectDirectory.file("scripts/docs.ps1")
val docsShellScript = layout.projectDirectory.file("scripts/docs.sh")
val isWindows = System.getProperty("os.name").lowercase().contains("windows")

fun docsCommand(mode: String): List<String> =
    if (isWindows) {
        listOf(
            "powershell",
            "-NoProfile",
            "-ExecutionPolicy",
            "Bypass",
            "-File",
            docsPowerShellScript.asFile.absolutePath,
            mode,
            "-Port",
            docsPort.get(),
            "-ContainerCommand",
            docsContainerCommand.get()
        )
    } else {
        listOf(
            "bash",
            docsShellScript.asFile.absolutePath,
            mode,
            "--port",
            docsPort.get(),
            "--container-command",
            docsContainerCommand.get()
        )
    }

tasks.register<Exec>("doc-build") {
    group = "documentation"
    description = "Builds the complete static Markdown documentation site through Docker or Podman."

    inputs.dir("docs")
    inputs.file("docs-site/Dockerfile")
    inputs.file("docs-site/package.json")
    inputs.file("docs-site/docusaurus.config.js")
    inputs.file("docs-site/sidebars.js")
    inputs.file(if (isWindows) docsPowerShellScript else docsShellScript)
    outputs.dir("docs-site/build")

    commandLine(docsCommand("build"))
}

tasks.register<Exec>("doc-dev") {
    group = "documentation"
    description = "Serves the Markdown documentation site in development mode with live rebuilds."

    inputs.dir("docs")
    inputs.file("docs-site/Dockerfile")
    inputs.file("docs-site/package.json")
    inputs.file("docs-site/docusaurus.config.js")
    inputs.file("docs-site/sidebars.js")
    inputs.file(if (isWindows) docsPowerShellScript else docsShellScript)

    commandLine(docsCommand("dev"))
}
