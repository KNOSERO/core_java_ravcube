param(
    [Parameter(Mandatory = $true, Position = 0)]
    [ValidateSet("build", "dev")]
    [string] $Mode,

    [string] $Port = "3000",

    [string] $ContainerCommand = ""
)

$ErrorActionPreference = "Stop"

$Image = "core-java-ravcube-docs:local"
$ContainerName = "core-java-ravcube-docs-dev"
$ContainerLabel = "com.ravcube.docs=core-java-ravcube"
$WorkspacePath = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..")).Path.Replace("\", "/")
$DefaultDocker = "C:\Program Files\Docker\Docker\resources\bin\docker.exe"
$LocalAppData = if ($env:LOCALAPPDATA) { $env:LOCALAPPDATA } else { "" }
$DefaultPodman = Join-Path $LocalAppData "Programs\Podman\podman.exe"

function Test-ContainerCommand {
    param([string] $Command)

    if ([string]::IsNullOrWhiteSpace($Command)) {
        return $false
    }

    $exists = if ($Command.Contains("\") -or $Command.Contains("/")) {
        Test-Path -LiteralPath $Command -PathType Leaf
    } else {
        $null -ne (Get-Command $Command -ErrorAction SilentlyContinue)
    }

    if (-not $exists) {
        return $false
    }

    $previousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"

    try {
        & $Command info *> $null
        return $LASTEXITCODE -eq 0
    } catch {
        return $false
    } finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }
}

function Test-PodmanCommand {
    param([string] $Command)

    if (-not (Test-ContainerCommand $Command)) {
        $previousErrorActionPreference = $ErrorActionPreference
        $ErrorActionPreference = "Continue"

        try {
            & $Command machine start *> $null
        } catch {
        } finally {
            $ErrorActionPreference = $previousErrorActionPreference
        }
    }

    return Test-ContainerCommand $Command
}

function Resolve-ContainerCommand {
    if (-not [string]::IsNullOrWhiteSpace($ContainerCommand)) {
        $isPodman = (Split-Path -Leaf $ContainerCommand).ToLowerInvariant().StartsWith("podman")
        $works = if ($isPodman) { Test-PodmanCommand $ContainerCommand } else { Test-ContainerCommand $ContainerCommand }

        if ($works) {
            return $ContainerCommand
        }

        throw "Configured container runtime is not available or not running: $ContainerCommand"
    }

    $candidates = @(
        "docker",
        $DefaultDocker,
        "podman",
        $DefaultPodman
    ) | Select-Object -Unique

    foreach ($candidate in $candidates) {
        $isPodman = (Split-Path -Leaf $candidate).ToLowerInvariant().StartsWith("podman")
        $works = if ($isPodman) { Test-PodmanCommand $candidate } else { Test-ContainerCommand $candidate }

        if ($works) {
            return $candidate
        }
    }

    throw @"
No running container runtime was found.

Start Docker Desktop or Podman machine, or pass the executable explicitly:
.\gradlew.bat doc-build -PcontainerCommand="C:\Program Files\Docker\Docker\resources\bin\docker.exe"
.\gradlew.bat doc-dev -PcontainerCommand="C:\Users\<you>\AppData\Local\Programs\Podman\podman.exe"
"@
}

function Invoke-Container {
    param(
        [string] $Command,
        [string[]] $Arguments
    )

    & $Command @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Container command failed with exit code ${LASTEXITCODE}: $Command $($Arguments -join ' ')"
    }
}

function Invoke-ContainerIgnoringFailure {
    param(
        [string] $Command,
        [string[]] $Arguments
    )

    & $Command @Arguments *> $null
}

function Get-ContainerOutput {
    param(
        [string] $Command,
        [string[]] $Arguments
    )

    $output = & $Command @Arguments 2>$null
    if ($LASTEXITCODE -eq 0) {
        return $output
    }

    return @()
}

function Get-ContainerIdsPublishingPort {
    param([string] $Command)

    $rows = Get-ContainerOutput $Command @("ps", "--format", "{{.ID}} {{.Ports}}")
    return $rows |
        ForEach-Object { "$_".Trim() } |
        Where-Object { $_ -match "[:\.]$Port->" -or $_ -match "\s$Port->" } |
        ForEach-Object { ($_ -split "\s+")[0] } |
        Where-Object { $_ }
}

function Build-DocsImage {
    param([string] $Command)

    Invoke-Container $Command @("build", "-f", "docs-site/Dockerfile", "-t", $Image, ".")
}

function Stop-DocsContainers {
    param([string] $Command)

    Write-Host "Stopping previous documentation containers for port $Port if they exist."
    Invoke-ContainerIgnoringFailure $Command @("rm", "-f", $ContainerName)

    $ids = @()
    $ids += Get-ContainerOutput $Command @("ps", "-aq", "--filter", "label=$ContainerLabel")
    $ids += Get-ContainerIdsPublishingPort $Command
    $ids = $ids | ForEach-Object { "$_".Trim() } | Where-Object { $_ } | Select-Object -Unique

    if ($ids.Count -gt 0) {
        Invoke-Container $Command (@("rm", "-f") + $ids)
    }
}

function Invoke-StaticSiteBuild {
    param([string] $Command)

    Invoke-Container $Command @(
        "run",
        "--rm",
        "--mount",
        "type=bind,source=$WorkspacePath,target=/workspace",
        "-w",
        "/workspace/docs-site",
        $Image,
        "docusaurus",
        "build"
    )
}

function Invoke-DocsDev {
    param([string] $Command)

    Write-Host "Documentation will be available at http://127.0.0.1:$Port"
    Write-Host "Do not open http://0.0.0.0:3000; that address is only the container bind address."

    Invoke-Container $Command @(
        "run",
        "--rm",
        "--name",
        $ContainerName,
        "--label",
        $ContainerLabel,
        "-p",
        "127.0.0.1:${Port}:3000",
        "-e",
        "BROWSER=none",
        "--mount",
        "type=bind,source=$WorkspacePath,target=/workspace",
        "-w",
        "/workspace/docs-site",
        $Image,
        "docusaurus",
        "start",
        "--host",
        "0.0.0.0",
        "--port",
        "3000"
    )
}

$ResolvedCommand = Resolve-ContainerCommand
Build-DocsImage $ResolvedCommand

if ($Mode -eq "build") {
    Invoke-StaticSiteBuild $ResolvedCommand
} else {
    Stop-DocsContainers $ResolvedCommand
    Invoke-DocsDev $ResolvedCommand
}
