val docsPort = providers.gradleProperty("docsPort").orElse("3000")
val docsContainerCommand = providers.gradleProperty("containerCommand").orElse("")
val docsScript = layout.projectDirectory.file("scripts/docs.ps1")

tasks.register<Exec>("doc-build") {
    group = "documentation"
    description = "Builds the complete static Markdown documentation site through Docker or Podman."

    inputs.dir("docs")
    inputs.file("docs-site/Dockerfile")
    inputs.file("docs-site/package.json")
    inputs.file("docs-site/docusaurus.config.js")
    inputs.file("docs-site/sidebars.js")
    inputs.file(docsScript)
    outputs.dir("docs-site/build")

    commandLine(
        "powershell",
        "-NoProfile",
        "-ExecutionPolicy",
        "Bypass",
        "-File",
        docsScript.asFile.absolutePath,
        "build",
        "-Port",
        docsPort.get(),
        "-ContainerCommand",
        docsContainerCommand.get()
    )
}

tasks.register<Exec>("doc-dev") {
    group = "documentation"
    description = "Serves the Markdown documentation site in development mode with live rebuilds."

    inputs.dir("docs")
    inputs.file("docs-site/Dockerfile")
    inputs.file("docs-site/package.json")
    inputs.file("docs-site/docusaurus.config.js")
    inputs.file("docs-site/sidebars.js")
    inputs.file(docsScript)

    commandLine(
        "powershell",
        "-NoProfile",
        "-ExecutionPolicy",
        "Bypass",
        "-File",
        docsScript.asFile.absolutePath,
        "dev",
        "-Port",
        docsPort.get(),
        "-ContainerCommand",
        docsContainerCommand.get()
    )
}
