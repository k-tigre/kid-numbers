# Build, tests, and CI

**Requirements:** JDK 21, Android SDK.

## Commands

```bash
./gradlew :androidApp:assembleDebug          # debug (by.tigre.numbers.dev)
./gradlew :androidApp:assembleQa             # QA build (ProGuard, like CI)
./gradlew testQaUnitTest testDebugUnitTest   # run before PR
./gradlew :androidApp:buildMarketingScreenshots  # Play screenshots
```

Debug signing: `keys/debug.jks` (password `debug123`).

## Build variants (`buildSrc/Environment.kt`)

| Variant | applicationId suffix | ProGuard | Analytics |
|---------|---------------------|----------|-----------|
| debug | `.dev` | no | off |
| qa | `.dev` | yes | on |
| release | none | yes | on |

Version source of truth: `buildSrc/src/main/kotlin/Application.kt`.

## logger dependency

`com.github.k-tigre:logger` from GitHub Packages. Set `gpr.user`/`gpr.key` in `gradle.properties` or `GITHUB_ACTOR`/`GITHUB_TOKEN`.

## Tests

Location: `androidApp/src/testDebug/`.

| Package | Purpose |
|---------|---------|
| `leaderboard/` | `LeaderboardBoardKey` unit tests |
| `marketing/` | Roborazzi screenshots (RU/EN) |

Screenshot script: `scripts/build-marketing-screenshots.ps1`.

## CI workflows

| Workflow | Trigger | Action |
|----------|---------|--------|
| `build_main.yml` | push to `main`/`master` | tests + QA → Firebase App Distribution |
| `build_release.yml` | tag `v.X.Y.Z` | tests + AAB → Play alpha 50% |
| `publish_play_listing.yml` | manual | listing only (texts, screenshots) |

**App release and Play listing are separate pipelines.**

## Releases

Use the skills — they contain the full step-by-step:

- **App release** → [.cursor/skills/prepare-release/SKILL.md](../../.cursor/skills/prepare-release/SKILL.md)
- **Play listing** → [.cursor/skills/prepare-play-listing/SKILL.md](../../.cursor/skills/prepare-play-listing/SKILL.md)

## Other docs

| File | Contents |
|------|----------|
| [leaderboard-score.md](../leaderboard-score.md) | Rating formula |
| [marketing/play-listing.md](../marketing/play-listing.md) | Play Store text source |
| [marketing/screenshot-build.md](../marketing/screenshot-build.md) | Screenshot pipeline |
| [CHANGELOG.md](../../CHANGELOG.md) | Release history (RU/EN) |
