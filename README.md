# core_java_ravcube

Reusable Spring integration libraries and test modules.

## Documentation

The documentation entry point is [docs/index.md](docs/index.md). It explains
the module map, public library guides, test modules, and local site workflow.

Serve the documentation locally:

```powershell
.\\gradlew.bat doc-dev
```

On Linux or macOS:

```bash
./gradlew doc-dev
```

`doc-dev` stops the previous documentation container for the selected port before
starting a new one.

Open:

```text
http://127.0.0.1:3000
```

If Docusaurus prints `http://0.0.0.0:3000` in container logs, do not open that
URL. Use `http://127.0.0.1:3000` from your browser.

Use a different port:

```powershell
.\\gradlew.bat doc-dev -PdocsPort=8010
```

Build a static website:

```powershell
.\\gradlew.bat doc-build
```

On Linux or macOS:

```bash
./gradlew doc-build
```

Documentation tooling runs in Docker or Podman. A local Python installation is
not required.

JetBrains users can run the Gradle tasks directly from the Gradle tool window:

- `doc-dev` - serves the Markdown documentation site locally with live rebuilds.
- `doc-build` - builds the complete static Markdown documentation site.

Docker Desktop or Podman must be installed.

If Gradle runs from an environment that does not inherit your PATH, pass the
container executable explicitly:

```powershell
.\\gradlew.bat doc-dev -PcontainerCommand="C:\\Program Files\\Docker\\Docker\\resources\\bin\\docker.exe"
```

On Linux or macOS:

```bash
./gradlew doc-dev -PcontainerCommand=docker
```

