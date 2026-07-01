param(
    [string]$Revision,

    [string]$RepositoryRoot,

    [string]$CentralUsername = $env:MAVEN_CENTRAL_USERNAME,
    [string]$CentralToken = $env:MAVEN_CENTRAL_TOKEN,
    [string]$GpgPrivateKeyFile = $env:GPG_PRIVATE_KEY_FILE,
    [string]$GpgPrivateKey = $env:GPG_PRIVATE_KEY,
    [string]$GpgPassphrase = $env:GPG_PASSPHRASE
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($RepositoryRoot)) {
    $scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
    $RepositoryRoot = (Resolve-Path (Join-Path $scriptRoot "..")).Path
}

if ([string]::IsNullOrWhiteSpace($Revision)) {
    $pomPath = Join-Path $RepositoryRoot "pom.xml"
    if (-not (Test-Path $pomPath)) {
        throw "Could not find root pom.xml at $pomPath"
    }

    $pomContent = Get-Content -Path $pomPath -Raw
    $revisionMatch = [regex]::Match($pomContent, '<revision>([^<]+)</revision>')
    if (-not $revisionMatch.Success) {
        throw "Could not determine revision from $pomPath"
    }

    $Revision = $revisionMatch.Groups[1].Value.Trim()
}

if ($Revision.EndsWith("-SNAPSHOT")) {
    throw "Release revision must not be a SNAPSHOT: $Revision"
}

$GpgExecutable = @(
    "C:\Program Files\GnuPG\bin\gpg.exe",
    "C:\Program Files (x86)\GnuPG\bin\gpg.exe"
) | Where-Object { Test-Path $_ } | Select-Object -First 1

if ([string]::IsNullOrWhiteSpace($GpgExecutable)) {
    $gpgCommand = Get-Command "gpg" -ErrorAction SilentlyContinue
    if ($null -ne $gpgCommand) {
        $GpgExecutable = $gpgCommand.Source
    }
}

$publishModules = @(
    "spring-boot/server/spring-boot-server-autoconfigure",
    "spring-boot/server/rest/spring-boot-server-rest",
    "spring-boot/server/rest/spring-boot-starter-server-rest"
)

function Assert-Command {
    param([string]$Name)
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Required command not found: $Name"
    }
}

Assert-Command -Name "mvn"
if ([string]::IsNullOrWhiteSpace($GpgExecutable)) {
    throw "Required command not found: gpg"
}

if ([string]::IsNullOrWhiteSpace($CentralUsername)) {
    throw "MAVEN_CENTRAL_USERNAME is required."
}

if ([string]::IsNullOrWhiteSpace($CentralToken)) {
    throw "MAVEN_CENTRAL_TOKEN is required."
}

if ([string]::IsNullOrWhiteSpace($GpgPrivateKey) -and [string]::IsNullOrWhiteSpace($GpgPrivateKeyFile)) {
    throw "Set GPG_PRIVATE_KEY or GPG_PRIVATE_KEY_FILE."
}

$tempRoot = Join-Path $env:TEMP ("spring-a2a-central-" + [Guid]::NewGuid().ToString("N"))
New-Item -ItemType Directory -Path $tempRoot | Out-Null

$settingsPath = Join-Path $tempRoot "settings.xml"
$gpgKeyPath = Join-Path $tempRoot "private-key.asc"

try {
    @"
<settings>
  <servers>
    <server>
      <id>central</id>
      <username>$CentralUsername</username>
      <password>$CentralToken</password>
    </server>
  </servers>
</settings>
"@ | Set-Content -Path $settingsPath -Encoding utf8

    if (-not [string]::IsNullOrWhiteSpace($GpgPrivateKeyFile)) {
        Copy-Item -Path $GpgPrivateKeyFile -Destination $gpgKeyPath
    }
    else {
        $GpgPrivateKey | Set-Content -Path $gpgKeyPath -Encoding utf8
    }

    $gpgArguments = @("--batch", "--yes")
    if (-not [string]::IsNullOrWhiteSpace($GpgPassphrase)) {
        $gpgArguments += @("--pinentry-mode", "loopback", "--passphrase", $GpgPassphrase)
    }
    $gpgArguments += @("--import", $gpgKeyPath)

    $gpgImportStdout = Join-Path $tempRoot "gpg-import.out.log"
    $gpgImportStderr = Join-Path $tempRoot "gpg-import.err.log"
    $gpgProcess = Start-Process -FilePath $GpgExecutable -ArgumentList $gpgArguments -NoNewWindow -Wait -PassThru -RedirectStandardOutput $gpgImportStdout -RedirectStandardError $gpgImportStderr
    $gpgStatus = @(
        Get-Content -Path $gpgImportStdout -Raw -ErrorAction SilentlyContinue
        Get-Content -Path $gpgImportStderr -Raw -ErrorAction SilentlyContinue
    ) -join "`n"
    if ($gpgProcess.ExitCode -ne 0) {
        throw "GPG import failed:`n$gpgStatus"
    }

    Push-Location $RepositoryRoot
    try {
        $moduleList = $publishModules -join ","
        $mvnArguments = @(
            "-B",
            "-s", $settingsPath,
            "-Prelease-assets,central-release",
            "-Drevision=$Revision",
            "-Dgpg.executable=$GpgExecutable",
            "-Dgpg.homedir=$env:APPDATA\gnupg",
            "-pl", $moduleList,
            "-am",
            "-DskipTests",
            "deploy"
        )
        & mvn @mvnArguments
        if ($LASTEXITCODE -ne 0) {
            throw "Maven deploy failed."
        }
    }
    finally {
        Pop-Location
    }
}
finally {
    Remove-Item -LiteralPath $tempRoot -Recurse -Force -ErrorAction SilentlyContinue
}
