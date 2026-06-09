# Record app screenshots (Roborazzi) and rebuild Google Play marketing assets.
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
Write-Host "Recording screenshots..."
& .\gradlew.bat :androidApp:recordRoborazziDebug
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Write-Host "Building marketing assets..."
Set-Location "docs\marketing\assets"
python scripts\build_assets.py
exit $LASTEXITCODE
