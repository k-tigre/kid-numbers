# Upload Play Store listing (texts, screenshots) without publishing a release.
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
& .\gradlew.bat :androidApp:publishPlayListing
exit $LASTEXITCODE
