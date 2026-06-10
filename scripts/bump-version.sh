#!/usr/bin/env bash
set -euo pipefail

VERSION="${RELEASE_VERSION:-}"
if [[ -z "$VERSION" ]]; then
    TAG="${GITHUB_REF_NAME:-}"
    TAG="${TAG#refs/tags/}"
    TAG="${TAG#v.}"
    VERSION="$TAG"
fi
if [[ ! "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "Invalid release version: '${VERSION}' (expected X.Y.Z or tag v.X.Y.Z)" >&2
    exit 1
fi
python scripts/changelog_tool.py bump-version --version "$VERSION"
