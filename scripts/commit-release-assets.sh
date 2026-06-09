#!/usr/bin/env bash
set -euo pipefail

VERSION="${RELEASE_VERSION:-}"
if [[ -z "$VERSION" ]]; then
    TAG="${GITHUB_REF_NAME:-}"
    TAG="${TAG#refs/tags/}"
    TAG="${TAG#v.}"
    VERSION="$TAG"
fi
BRANCH="${RELEASE_BRANCH:-master}"
COMMIT_MESSAGE="Release v.${VERSION}: marketing assets and version bump"
git config user.name "github-actions[bot]"
git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
git add \
    CHANGELOG.md \
    buildSrc/src/main/kotlin/Application.kt \
    androidApp/src/main/play/release-notes/ \
    androidApp/src/main/play/listings/ \
    docs/marketing/assets/output/
if git diff --staged --quiet; then
    echo "No release asset changes to commit."
    exit 0
fi
git commit -m "$COMMIT_MESSAGE"
git push origin "HEAD:${BRANCH}"
