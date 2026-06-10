#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
echo "Syncing Play listing texts from docs/marketing/play-listing.md..."
python scripts/sync-play-listing-texts.py
echo "Recording screenshots and building marketing assets..."
./gradlew :androidApp:recordMarketingScreenshots
