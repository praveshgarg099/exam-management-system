<#
.SYNOPSIS
    Exam Management System - Windows Installer Build Orchestrator
.DESCRIPTION
    Automates the complete Windows desktop application packaging workflow:
    1. Compiles Java source files and assembles dist\exam-management-system.jar
    2. Builds a lean (~50MB) custom JRE runtime via jlink
    3. Bundles portable PostgreSQL binaries (if staged or requested)
    4. Compiles Inno Setup script into dist\installer\ExamManagementSystem-Setup.exe
.PARAMETER SkipPostgres
    If specified, skips bundling portable PostgreSQL binaries (installer will expect system/remote DB)
.PARAMETER SkipRuntime
    If specified, skips rebuilding runtime\ if already present
#>

[CmdletBinding()]
param(
    [switch]$SkipPostgres,
    [switch]$SkipRuntime
)

$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$ProjectRoot = Resolve-Path (Join-Path $ScriptDir "..\..\..")
Set-Location $ProjectRoot

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "    Exam Management System - Windows Packaging Pipeline     " -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "Project Root : $ProjectRoot"

# -----------------------------------------------------------------------------
# 1. Environment Verification
# -----------------------------------------------------------------------------
Write-Host "`n[Step 1/5] Verifying environment and build tools..." -ForegroundColor Yellow

# Locate javac
$javacCmd = Get-Command javac -ErrorAction SilentlyContinue
if (-not $javacCmd -and $env:JAVA_HOME) {
    $javacCandidate = Join-Path $env:JAVA_HOME "bin\javac.exe"
    if (Test-Path $javacCandidate) { $javacCmd = $javacCandidate }
}
if (-not $javacCmd) {
    throw "ERROR: 'javac' not found. Please ensure JDK 19+ is installed and configured in PATH or JAVA_HOME."
}
Write-Host "  -> Java Compiler: $javacCmd"

# Locate jlink
$jlinkCmd = Get-Command jlink -ErrorAction SilentlyContinue
if (-not $jlinkCmd -and $env:JAVA_HOME) {
    $jlinkCandidate = Join-Path $env:JAVA_HOME "bin\jlink.exe"
    if (Test-Path $jlinkCandidate) { $jlinkCmd = $jlinkCandidate }
}
if (-not $jlinkCmd) {
    throw "ERROR: 'jlink' not found. Please ensure JDK 19+ is installed."
}
Write-Host "  -> jlink tool   : $jlinkCmd"

# Locate Inno Setup Compiler (ISCC.exe)
$isccCandidates = @(
    (Get-Command ISCC.exe -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Source -ErrorAction SilentlyContinue),
    "${env:ProgramFiles(x86)}\Inno Setup 6\ISCC.exe",
    "${env:ProgramFiles}\Inno Setup 6\ISCC.exe"
)
$isccPath = $null
foreach ($cand in $isccCandidates) {
    if ($cand -and (Test-Path $cand)) {
        $isccPath = $cand
        break
    }
}
if (-not $isccPath) {
    Write-Warning "Inno Setup 6 (ISCC.exe) not detected. Installer compilation will be skipped if ISCC is absent."
} else {
    Write-Host "  -> Inno Setup   : $isccPath"
}

# -----------------------------------------------------------------------------
# 2. Build Application JAR
# -----------------------------------------------------------------------------
Write-Host "`n[Step 2/5] Building application JAR..." -ForegroundColor Yellow
$buildJarBat = Join-Path $ProjectRoot "packaging\windows\scripts\build-app-jar.bat"
if (Test-Path $buildJarBat) {
    & cmd.exe /c $buildJarBat
    if ($LASTEXITCODE -ne 0) { throw "build-app-jar.bat failed with exit code $LASTEXITCODE" }
} else {
    throw "Missing $buildJarBat"
}

# -----------------------------------------------------------------------------
# 3. Build Custom Modular Runtime
# -----------------------------------------------------------------------------
Write-Host "`n[Step 3/5] Building custom modular JRE runtime..." -ForegroundColor Yellow
$runtimeDir = Join-Path $ProjectRoot "target\runtime"
if ($SkipRuntime -and (Test-Path (Join-Path $runtimeDir "bin\java.exe"))) {
    Write-Host "  -> Runtime already present at $runtimeDir (skipping rebuild)."
} else {
    $buildRuntimeBat = Join-Path $ProjectRoot "packaging\windows\scripts\build-runtime.bat"
    if (Test-Path $buildRuntimeBat) {
        & cmd.exe /c $buildRuntimeBat
        if ($LASTEXITCODE -ne 0) { throw "build-runtime.bat failed with exit code $LASTEXITCODE" }
    } else {
        throw "Missing $buildRuntimeBat"
    }
}

# -----------------------------------------------------------------------------
# 4. Stage Portable PostgreSQL (Optional / If Requested)
# -----------------------------------------------------------------------------
Write-Host "`n[Step 4/5] Checking portable PostgreSQL staging..." -ForegroundColor Yellow
$pgsqlDir = Join-Path $ProjectRoot "dist\pgsql"
if ($SkipPostgres) {
    Write-Host "  -> Skipping PostgreSQL bundling (-SkipPostgres specified)."
} elseif (Test-Path (Join-Path $pgsqlDir "bin\pg_ctl.exe")) {
    Write-Host "  -> Bundled PostgreSQL already staged at $pgsqlDir."
} else {
    Write-Host "  -> Notice: dist\pgsql\bin\pg_ctl.exe not found."
    Write-Host "     To bundle PostgreSQL with the installer, place portable PostgreSQL binaries in dist\pgsql\"
    Write-Host "     or download the Windows binaries from EnterpriseDB/PostgreSQL community."
}

# -----------------------------------------------------------------------------
# 5. Compile Inno Setup Installer
# -----------------------------------------------------------------------------
Write-Host "`n[Step 5/5] Compiling Inno Setup 6 standalone installer..." -ForegroundColor Yellow
if (-not $isccPath) {
    Write-Host "SKIPPING: Inno Setup 6 compiler (ISCC.exe) not found on this system." -ForegroundColor Yellow
    Write-Host "To generate ExamManagementSystem-Setup.exe on Windows:"
    Write-Host "  1. Download Inno Setup 6 from https://jrsoftware.org/isinfo.php"
    Write-Host "  2. Run: ISCC.exe packaging\windows\inno-setup\ExamManagementSystem.iss"
} else {
    $issFile = Join-Path $ProjectRoot "packaging\windows\inno-setup\ExamManagementSystem.iss"
    & $isccPath $issFile
    if ($LASTEXITCODE -ne 0) { throw "Inno Setup compiler failed with exit code $LASTEXITCODE" }

    $installerPath = Join-Path $ProjectRoot "dist\installer\ExamManagementSystem-Setup.exe"
    if (Test-Path $installerPath) {
        $fileInfo = Get-Item $installerPath
        Write-Host "------------------------------------------------------------" -ForegroundColor Green
        Write-Host "SUCCESS: Installer generated successfully!" -ForegroundColor Green
        Write-Host "Path : $($fileInfo.FullName)"
        Write-Host "Size : $([Math]::Round($fileInfo.Length / 1MB, 2)) MB"
        Write-Host "------------------------------------------------------------" -ForegroundColor Green
    }
}

Write-Host "`nWindows Packaging Pipeline Complete!`n" -ForegroundColor Green
