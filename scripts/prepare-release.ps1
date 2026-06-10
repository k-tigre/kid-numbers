# Bump version, write Play release notes, and finalize CHANGELOG for a release.
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string]$Version
)
$ErrorActionPreference = "Stop"
if ($Version -notmatch '^\d+\.\d+\.\d+$') {
    Write-Error "Invalid version: '$Version' (expected X.Y.Z)"
}
$bash = Get-Command bash -ErrorAction SilentlyContinue
if (-not $bash) {
    Write-Error "bash not found. Install Git for Windows or run: bash scripts/prepare-release.sh $Version"
}
& bash scripts/prepare-release.sh $Version
exit $LASTEXITCODE
