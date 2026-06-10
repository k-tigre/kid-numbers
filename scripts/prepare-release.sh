#!/usr/bin/env bash
set -euo pipefail
VERSION="${1:-}"
if [[ -z "$VERSION" ]]; then
    echo "Usage: $0 X.Y.Z" >&2
    exit 1
fi
if [[ ! "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "Invalid version: '${VERSION}' (expected X.Y.Z)" >&2
    exit 1
fi
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
export RELEASE_VERSION="$VERSION"
./scripts/bump-version.sh
./scripts/generate-play-release-notes.sh alpha
python scripts/changelog_tool.py finalize --version "$VERSION"
