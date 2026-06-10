# Sync Play listing texts and rebuild marketing screenshots for Google Play.
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
Write-Host "Syncing Play listing texts from docs/marketing/play-listing.md..."
python scripts/sync-play-listing-texts.py
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Write-Host "Recording screenshots and building marketing assets..."
& .\gradlew.bat :androidApp:recordMarketingScreenshots
exit $LASTEXITCODE
