# Common tasks — where to change what

| Goal | Files / packages |
|------|------------------|
| Game UI | `presentation/game/GameView.kt`, `GameComponent.kt` |
| Per-type game settings | `presentation/game/settings/` |
| Question generation | `domain/GameProvider.kt`, `entity/GameSettings.kt` |
| Game time limits | `domain/GameDurationProvider.kt`, `data/remoteconfig/GameDurationConfig.kt`, `entity/Difficult.kt` |
| Post-game result screen | `presentation/game/result/` |
| Main menu | `presentation/menu/` |
| Challenge list / creator | `presentation/challenge/` |
| History | `presentation/history/`, `data/history/` |
| Statistics | `presentation/statistic/` |
| Leaderboard UI | `presentation/leaderboard/` |
| Leaderboard backend | `data/leaderboard/` |
| Leaderboard score logic | `docs/leaderboard-score.md`, Remote Config `leaderboard_rating_json` |
| App settings (nickname) | `presentation/settings/` |
| Navigation / new screen | `presentation/root/RootComponent.kt` |
| Dependency wiring | `di/ApplicationGraph.kt`, `di/*Module.kt` |
| Local DB schema | `androidApp/src/main/sqldelight/by/tigre/numbers/db/` |
| Feature flags | `data/remoteconfig/FeatureFlags.kt` |
| Analytics events | `analytics/Event.kt`, `analytics/Tracker` |
| Marketing screenshots | `presentation/screenshot/`, `androidApp/src/testDebug/.../marketing/` |
| Play listing texts | `docs/marketing/play-listing.md` → `scripts/prepare-play-listing.ps1` |
| Screenshot captions | `docs/marketing/assets/config.json` |
