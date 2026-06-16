# Coding conventions

1. **Minimal diff** — do not refactor unrelated code.
2. **Match existing patterns** — Component/View, `Impl` classes, `StateFlow`.
3. **Layer placement** — models in `entity/`, persistence in `data/`, logic in `domain/`, UI in `presentation/`.
4. **New screen** — Component + View + `RootComponent` registration + `di/` if needed. See [architecture.md](architecture.md).
5. **Strings** — `androidApp/src/main/res/values/strings.xml` and `values-ru/`. Always add both locales.
6. **Version** — change only via release process (`Application.kt` + `scripts/prepare-release.ps1`). See [build-and-ci.md](build-and-ci.md).
7. **Commits** — only when the user explicitly asks.

## SQLDelight

Schemas: `androidApp/src/main/sqldelight/by/tigre/numbers/db/`.

After schema changes, rebuild the project. Generated code lands in `androidApp/build/generated/sqldelight/` — do not edit.

## Analytics

Add events in `analytics/Event.kt`, route through `Tracker`. Respect `BuildConfig.REMOTE_ANALYTICS_ENABLED` — analytics is off in debug.
