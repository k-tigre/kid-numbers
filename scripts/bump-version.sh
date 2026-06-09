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
IFS='.' read -r MAJOR MINOR PATCH <<< "$VERSION"
APPLICATION_FILE="buildSrc/src/main/kotlin/Application.kt"
python - "$APPLICATION_FILE" "$MAJOR" "$MINOR" "$PATCH" <<'PY'
import re
import sys
from pathlib import Path

path = Path(sys.argv[1])
major, minor, patch = sys.argv[2:5]
text = path.read_text(encoding="utf-8")
updated, count = re.subn(
    r"Version\(\d+, \d+, \d+\)",
    f"Version({major}, {minor}, {patch})",
    text,
    count=1,
)
if count != 1:
    raise SystemExit(f"Failed to update version in {path}")
path.write_text(updated, encoding="utf-8")
print(f"Bumped version to {major}.{minor}.{patch}")
PY
