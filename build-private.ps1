param([switch]$LiveExtraction)
$ErrorActionPreference = 'Stop'
$project = $PSScriptRoot
if ($env:OS -eq 'Windows_NT' -and $project.StartsWith('\\')) {
    if (!$env:PRIVATESTREAM_WINDOWS_STAGE) {
        throw 'Building this WSL checkout from Windows requires PRIVATESTREAM_WINDOWS_STAGE to point to a local Windows directory.'
    }
    $stage = $env:PRIVATESTREAM_WINDOWS_STAGE
    $excluded = @('.git', '.gradle', '.kotlin', '.private-build', 'app\build', 'baselineprofile\build') | ForEach-Object { Join-Path $project $_ }
    robocopy $project $stage /E /R:1 /W:1 /NFL /NDL /NJH /NJS /NP /XD $excluded | Out-Null
    if ($LASTEXITCODE -ge 8) { throw "Unable to stage source for Windows build (robocopy $LASTEXITCODE)." }
    $arguments = if ($LiveExtraction) { @('-LiveExtraction') } else { @() }
    & (Join-Path $stage 'build-private.ps1') @arguments
    if ($LASTEXITCODE -ne 0) { throw "Gradle failed with exit code $LASTEXITCODE" }
    $builtApk = Join-Path $stage 'app\build\outputs\apk\debug\app-debug.apk'
    $workspaceApk = Join-Path $project 'app\build\outputs\apk\debug\app-debug.apk'
    New-Item -ItemType Directory -Force (Split-Path $workspaceApk) | Out-Null
    Copy-Item -LiteralPath $builtApk -Destination $workspaceApk -Force
    exit 0
}
$work = if ($env:PRIVATESTREAM_WORKDIR) { $env:PRIVATESTREAM_WORKDIR } else { Join-Path $project '.private-build' }
$toolchain = if ($env:PRIVATESTREAM_TOOLCHAIN_HOME) { $env:PRIVATESTREAM_TOOLCHAIN_HOME } else { Join-Path $work 'toolchain' }
$localJava = Join-Path $toolchain 'java'
if (!$env:JAVA_HOME -and (Test-Path $localJava)) {
    $env:JAVA_HOME = (Get-ChildItem $localJava -Directory | Select-Object -First 1).FullName
}
if (!$env:ANDROID_HOME) { $env:ANDROID_HOME = Join-Path $toolchain 'android-sdk' }
if (!$env:ANDROID_USER_HOME) { $env:ANDROID_USER_HOME = Join-Path $work 'android-user' }
if (!$env:GRADLE_USER_HOME) { $env:GRADLE_USER_HOME = Join-Path $work 'gradle' }
New-Item -ItemType Directory -Force $env:ANDROID_USER_HOME | Out-Null
$buildArgs = @(':app:assembleDebug', ':app:testDebugUnitTest', '--console=plain')
$localProtoc = Join-Path $toolchain 'protoc3\bin\protoc.exe'
if (Test-Path $localProtoc) { $buildArgs += "-PprivateStreamProtoc=$localProtoc" }
if ($LiveExtraction) {
    $buildArgs += '-PliveExtraction=true'
    $buildArgs += '--rerun-tasks'
}
Push-Location $project
try {
    & .\gradlew.bat @buildArgs
    if ($LASTEXITCODE -ne 0) { throw "Gradle failed with exit code $LASTEXITCODE" }
} finally { Pop-Location }
