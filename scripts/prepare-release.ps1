# Bump version, write Play release notes, and finalize CHANGELOG for a release.
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string]$Version
)
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
if ($Version -notmatch '^\d+\.\d+\.\d+$') {
    Write-Error "Invalid version: '$Version' (expected X.Y.Z)"
}
python scripts/changelog_tool.py bump-version --version $Version
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
python scripts/changelog_tool.py write-play-notes --version $Version --track alpha
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
python scripts/changelog_tool.py finalize --version $Version
exit $LASTEXITCODE
