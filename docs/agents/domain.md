# Domain

## Game types (`entity/GameType.kt`)

`Additional`, `Subtraction`, `Multiplication`, `Division`, `Equations`.

## Difficulty (`entity/Difficult.kt`)

| Level | Time limit |
|-------|------------|
| `Easy` | 180s |
| `Medium` | 120s |
| `Hard` | 90s |

## Game flow

```
Type settings → RootGameComponent → GameComponent → ResultComponent
                                      (timer, Q&A)    (history, leaderboard)
```

- Question generation: `domain/GameProvider.kt`
- Time limits: `domain/GameDurationProvider.kt`
- Settings per type: `entity/GameSettings.kt` (sealed classes)
- Results: `entity/GameResult.kt`

## Challenges

Task sets with deadlines: `TenMinutes` … `OneWeek`.

| Concern | Location |
|---------|----------|
| Model | `entity/Challenge.kt` |
| Storage | `data/challenges/ChallengesStore` (SQLDelight) |
| UI | `presentation/challenge/` |
| Reminders | `domain/reminder/` |

## Leaderboard

| Board | Collection | Meaning |
|-------|------------|---------|
| Speed | `leaderboard_speed` | Best time for a perfect run with exact settings |
| Total | `leaderboard_total` | Cumulative rating score |

- Board keys: `LeaderboardBoardKey` (e.g. `Mult_2-5-10_Easy`)
- Score formula: [leaderboard-score.md](../leaderboard-score.md)
- Firestore rules: [leaderboard-firestore-rules.txt](../leaderboard-firestore-rules.txt)
- Feature flag: `FeatureFlags.isLeaderboardEnabled`
- Nickname: `LeaderboardPreferences` / `presentation/settings/`

## Other

- **History & statistics** — `data/history/ResultStore`, `presentation/history/`, `presentation/statistic/`
- **Feature flags** — `data/remoteconfig/FeatureFlags` (leaderboard, purchases placeholder)
- **Analytics** — `analytics/`; disabled in debug via `BuildConfig.REMOTE_ANALYTICS_ENABLED`
