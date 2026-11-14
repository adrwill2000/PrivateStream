param([switch]$LiveExtraction)
$ErrorActionPreference = 'Stop'
$project = $PSScriptRoot
$work = Join-Path (Split-Path $project) 'work'
$localJava = Join-Path $work 'toolchain\java'
if (!$env:JAVA_HOME -and (Test-Path $localJava)) {
    $env:JAVA_HOME = (Get-ChildItem $localJava -Directory | Select-Object -First 1).FullName
}
if (!$env:ANDROID_HOME) { $env:ANDROID_HOME = Join-Path $work 'toolchain\android-sdk' }
$env:ANDROID_USER_HOME = Join-Path $work 'android-user'
$env:GRADLE_USER_HOME = Join-Path $work 'gradle'
New-Item -ItemType Directory -Force $env:ANDROID_USER_HOME | Out-Null
$buildArgs = @(':app:assembleDebug', ':app:testDebugUnitTest', '--console=plain')
$localProtoc = Join-Path $work 'toolchain\protoc3\bin\protoc.exe'
if (Test-Path $localProtoc) { $buildArgs += "-PprivateStreamProtoc=$localProtoc" }
if ($LiveExtraction) { $buildArgs += '-PliveExtraction=true' }
Push-Location $project
try {
    & .\gradlew.bat @buildArgs
    if ($LASTEXITCODE -ne 0) { throw "Gradle failed with exit code $LASTEXITCODE" }
} finally { Pop-Location }
