# Numbers

A free Android math trainer for kids aged 6–12. No ads. No subscriptions.

[Get it on Google Play](https://play.google.com/store/apps/details?id=by.tigre.numbers)

## Features

- **5 task types** — addition, subtraction, multiplication, division, equations
- **3 difficulty levels**
- **Timed practice** with instant feedback
- **Challenges** — task sets with deadlines from 10 minutes to 1 week
- **Statistics and history** of results
- **Online leaderboard** — total score and speed records for perfect runs

Available in English and Russian.

## Tech stack


| Layer       | Technologies                                                                  |
| ----------- | ----------------------------------------------------------------------------- |
| UI          | Jetpack Compose, Material 3                                                   |
| Navigation  | Decompose                                                                     |
| Concurrency | Kotlin Coroutines                                                             |
| Database    | SQLDelight                                                                    |
| Backend     | Firebase (Firestore, Remote Config, Analytics, Crashlytics, App Distribution) |
| Analytics   | Mixpanel                                                                      |
| Testing     | JUnit, Robolectric, Roborazzi                                                 |


- **Kotlin** 2.2 · **minSdk** 26 · **compileSdk** 36
- **Package:** `by.tigre.numbers`

## Repository layout

```
androidApp/          # Android app
tools/               # Shared modules (Compose, Decompose, coroutines)
docs/                # Documentation (marketing, leaderboard, screenshots)
scripts/             # Build and automation scripts
buildSrc/            # Dependency versions and build configuration
```

## Build and run

**Requirements:** JDK 21, Android SDK.

```bash
# Debug build (by.tigre.numbers.dev)
./gradlew :androidApp:assembleDebug

# QA build (ProGuard enabled, used in CI)
./gradlew :androidApp:assembleQa
```

Local debug signing uses `keys/debug.jks` (password `debug123`).

The `com.github.k-tigre:logger` dependency is fetched from GitHub Packages. If needed, set `gpr.user` / `gpr.key` in `gradle.properties` or `GITHUB_ACTOR` / `GITHUB_TOKEN` in your environment.

## Tests

```bash
./gradlew testQaUnitTest testDebugUnitTest
```

Marketing screenshots (Roborazzi + Pillow):

```powershell
./gradlew :androidApp:buildMarketingScreenshots
# or
.\scripts\build-marketing-screenshots.ps1
```

## Contributing

This project is maintained through pull requests.

1. Create a branch from `main`.
2. Make your changes and keep the diff focused.
3. Run tests locally: `./gradlew testQaUnitTest testDebugUnitTest`
4. Open a pull request against `main` with a short description of what changed and why.

CI runs on every push to `main` — it executes unit tests, builds the QA artifact, and uploads it to Firebase App Distribution.

## License

[GNU GPL v3](LICENSE)