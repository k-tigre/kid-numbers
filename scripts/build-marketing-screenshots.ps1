# Record app screenshots (Roborazzi) and rebuild Google Play marketing assets.
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
Write-Host "Recording screenshots and building marketing assets..."
& .\gradlew.bat :androidApp:recordMarketingScreenshots
exit $LASTEXITCODE
