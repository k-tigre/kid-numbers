# AGENTS.md

Guide for AI agents in the **Numbers** repo — a free Android math trainer for kids 6–12.

**Read only the sections you need.** Detailed docs live in [docs/agents/](docs/agents/).

## Essentials

| | |
|---|---|
| **Package** | `by.tigre.numbers` |
| **Version** | `buildSrc/src/main/kotlin/Application.kt` |
| **Locales** | `en`, `ru` |
| **License** | GNU GPL v3 |
| **Stack** | Kotlin, Compose, Decompose, SQLDelight, Firebase, Mixpanel |
| **Default branch** | `master` |

Overview for humans: [README.md](README.md).

## Repository layout

```
androidApp/     # App, SQLDelight, Play listing
tools/          # Shared compose, decompose, coroutines modules
buildSrc/       # Versions, SDK, build types
docs/           # Product & agent docs
scripts/        # Release and listing automation
.cursor/skills/ # Release and Play listing skills
```

Do not touch: `androidApp/build/`, generated SQLDelight output, `keys/`.

## Where to look

| Task | Read |
|------|------|
| New screen, navigation, layers, DI | [docs/agents/architecture.md](docs/agents/architecture.md) |
| Game types, challenges, leaderboard | [docs/agents/domain.md](docs/agents/domain.md) |
| Coding rules, strings, minimal diff | [docs/agents/conventions.md](docs/agents/conventions.md) |
| Build, tests, CI, releases | [docs/agents/build-and-ci.md](docs/agents/build-and-ci.md) |
| "Where do I change X?" | [docs/agents/common-tasks.md](docs/agents/common-tasks.md) |
| Premium UI polish spec | [docs/superpowers/specs/2026-08-28-premium-ui-polish-design.md](docs/superpowers/specs/2026-08-28-premium-ui-polish-design.md) |

**Skills** (use when the task matches — do not re-read full docs):

| Skill | When |
|-------|------|
| [.cursor/skills/prepare-release/](.cursor/skills/prepare-release/SKILL.md) | version bump, tag, app release |
| [.cursor/skills/prepare-play-listing/](.cursor/skills/prepare-play-listing/SKILL.md) | Play Store texts/screenshots |

## Hard rules

- Minimal diff; match existing Component/View patterns.
- Strings in `values/` and `values-ru/` for any new UI text.
- Run `./gradlew testQaUnitTest testDebugUnitTest` before finishing.
- **No commits, pushes, or tags** unless the user asks.
- **No Play listing updates** on every patch — only when marketing changes.
- Do not commit secrets or keystores.

## Checklist

- [ ] Component/View pattern followed
- [ ] `en` + `ru` strings (if UI text changed)
- [ ] Tests pass
- [ ] Version unchanged (unless releasing)
