---
name: prepare-release
description: >-
  Prepares a patch, minor, or major release: writes user-facing CHANGELOG.md
  since the last tag, bumps version, finalizes changelog, commits, creates
  v.X.Y.Z tag, and pushes to trigger CI. Use when the user asks to prepare
  the next release, bump version, tag, push, or says «подготовь следующую
  патч/минорную/мажорную версию».
---

# Prepare Release

Подготовка релиза приложения: changelog → версия → release notes → коммит → тэг → push → CI.

**Маркетинг (тексты листинга, скриншоты)** — отдельное действие: skill [prepare-play-listing](../prepare-play-listing/SKILL.md).

## Как устроен пайплайн

**Локально (человек / агент):**
1. CHANGELOG.md — секция `## [X.Y.Z]` с `### RU` / `### EN`
2. `scripts/prepare-release.ps1 X.Y.Z` — версия, release notes, дата в changelog
3. Коммит + тэг + push

**В CI** (`.github/workflows/build_release.yml`, триггер — push тэга `v.X.Y.Z`):
1. Тесты, сборка AAB/APK
2. Публикация в Google Play (alpha 50%): **только AAB + «Что нового»** из `play/release-notes/`

CI **не** обновляет листинг (описания, скриншоты) — это отдельный workflow [publish_play_listing.yml](../../.github/workflows/publish_play_listing.yml).

## Шаг 0. Подготовка

```powershell
git checkout master
git pull origin master
git tag -l --sort=-v:refname | Select-Object -First 3
```

Последний релизный тэг — база для changelog и вычисления следующей версии.

Формат тэгов: `v.X.Y.Z` (с точкой после `v`, например `v.1.3.0`).

## Шаг 1. Вычисли следующую версию

От последнего тэга `v.A.B.C` → версия `A.B.C`:

| Тип | Когда | Формула | Пример (после `v.1.3.0`) |
|-----|-------|---------|--------------------------|
| **Patch** | багфиксы, мелкие правки | `A.B.(C+1)` | `1.3.1` |
| **Minor** | новые фичи, обратно совместимо | `A.(B+1).0` | `1.4.0` |
| **Major** | ломающие изменения, крупный редизайн | `(A+1).0.0` | `2.0.0` |

Если пользователь не указал тип — уточни: patch / minor / major.

## Шаг 2. Составь changelog

Анализируй изменения **с последнего релизного тэга** до `HEAD`:

```powershell
git log v.A.B.C..HEAD --oneline --no-merges
git diff v.A.B.C..HEAD --stat
```

### Формат в CHANGELOG.md

```markdown
## [X.Y.Z]

### RU
- …

### EN
- …
```

- Секцию `## [X.Y.Z]` вставь **сразу после** `## [Unreleased]`.
- Дату в заголовок **не ставь** — `prepare-release` добавит при `finalize`.
- Очисти `## [Unreleased]` (пустые `### RU` / `### EN`).
- Текст — **для пользователя**, не для разработчиков.

### Дефолт

Если с прошлого тэга нет изменений, заметных пользователю:

```markdown
### RU
- Правка багов и улучшения

### EN
- Bug fixes and improvements
```

### Что включать / исключать

**Включай:** новые функции, UI, исправления видимых багов, настройки, баланс, покупки, лидерборд.

**Исключай:** CI/CD, скрипты релиза, рефакторинг, тесты, зависимости, `bump version`, маркетинговый пайплайн.

| Плохо | Хорошо |
|-------|--------|
| Firebase Remote Config для лидерборда | Таблица лидеров теперь можно включать без обновления приложения |
| Fix Roborazzi tests on CI | — (не пишем) |

Правила текста: простой язык, 1–4 пункта, ≤ 500 символов на локаль.

### Проверка

```powershell
python scripts/changelog_tool.py extract --version X.Y.Z --locale ru-RU
python scripts/changelog_tool.py extract --version X.Y.Z --locale en-US
```

## Шаг 3. Примени версию и release notes

```powershell
.\scripts\prepare-release.ps1 X.Y.Z
```

Скрипт обновляет:
- `buildSrc/src/main/kotlin/Application.kt`
- `androidApp/src/main/play/release-notes/`
- дату в `CHANGELOG.md`

## Шаг 4. Закоммить

```powershell
git add CHANGELOG.md buildSrc/src/main/kotlin/Application.kt androidApp/src/main/play/release-notes/
git commit -m "Release v.X.Y.Z"
```

## Шаг 5. Тэг и push

```powershell
git tag v.X.Y.Z
git push origin master
git push origin v.X.Y.Z
```

CI запустится на push тэга. Дождись зелёного workflow **Release** в GitHub Actions.

## Чеклисты по типу релиза

### Patch (`1.3.0` → `1.3.1`)

```
- [ ] master актуален
- [ ] Версия = patch bump от последнего тэга
- [ ] Changelog: багфиксы / мелкие улучшения (или дефолт)
- [ ] prepare-release.ps1 X.Y.Z
- [ ] Коммит + тэг v.X.Y.Z + push master + push tag
```

### Minor (`1.3.0` → `1.4.0`)

```
- [ ] master актуален
- [ ] Версия = minor bump (patch → 0)
- [ ] Changelog: новые пользовательские фичи с прошлого тэга
- [ ] prepare-release.ps1 X.Y.Z
- [ ] Коммит + тэг v.X.Y.Z + push master + push tag
```

### Major (`1.3.0` → `2.0.0`)

```
- [ ] master актуален
- [ ] Версия = major bump (minor и patch → 0)
- [ ] Changelog: крупные изменения, понятные пользователю
- [ ] prepare-release.ps1 X.Y.Z
- [ ] Коммит + тэг v.X.Y.Z + push master + push tag
```

## Пример: minor-релиз

Последний тэг `v.1.2.3`, запрос «подготовь следующую минорную версию» → `1.3.0`.

```markdown
## [1.3.0]

### RU
- Добавлена настройка громкости звуков в игре
- Исправлен сбой при повороте экрана во время раунда

### EN
- Added in-game sound volume settings
- Fixed a crash when rotating the screen during a round
```

```powershell
.\scripts\prepare-release.ps1 1.3.0
git add CHANGELOG.md buildSrc/src/main/kotlin/Application.kt androidApp/src/main/play/release-notes/
git commit -m "Release v.1.3.0"
git tag v.1.3.0
git push origin master
git push origin v.1.3.0
```

## Связанные файлы

- [CHANGELOG.md](../../CHANGELOG.md)
- [buildSrc/src/main/kotlin/Application.kt](../../buildSrc/src/main/kotlin/Application.kt)
- [scripts/prepare-release.ps1](../../scripts/prepare-release.ps1)
- [scripts/changelog_tool.py](../../scripts/changelog_tool.py)
- [.github/workflows/build_release.yml](../../.github/workflows/build_release.yml)
- [prepare-play-listing](../prepare-play-listing/SKILL.md) — тексты и скриншоты Play
