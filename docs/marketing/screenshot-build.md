# Screenshot build

Съёмка скриншотов Google Play без эмулятора — через Roborazzi (JVM + Robolectric).

## Два локальных действия

| Действие | Когда | Скрипт / skill |
|----------|-------|----------------|
| **Листинг Play** (тексты + скриншоты) | Изменился UI или описание в Store | `scripts/prepare-play-listing.ps1`, skill `prepare-play-listing` |
| **Релиз приложения** (версия + changelog) | Новая версия для пользователей | `scripts/prepare-release.ps1`, skill `prepare-release` |

Действия **независимы**: листинг можно обновить без релиза, релиз — без пересъёмки скриншотов.

### Обновить листинг (тексты + скриншоты)

1. Тексты: [play-listing.md](play-listing.md) (`title`, `short-description`, `full-description` для `ru-RU` / `en-US`)
2. Подписи на скриншотах: [assets/config.json](assets/config.json)
3. Запуск:

```powershell
.\scripts\prepare-play-listing.ps1
git add docs/marketing/play-listing.md androidApp/src/main/play/listings/ docs/marketing/assets/output/
git commit -m "Update Play listing texts and screenshots"
git push origin master
```

### Подготовить релиз приложения

1. Секция `## [X.Y.Z]` в [CHANGELOG.md](../../CHANGELOG.md) (`### RU` / `### EN`)
2. Запуск:

```powershell
.\scripts\prepare-release.ps1 X.Y.Z
git add CHANGELOG.md buildSrc/src/main/kotlin/Application.kt androidApp/src/main/play/release-notes/
git commit -m "Release v.X.Y.Z"
git tag v.X.Y.Z
git push origin master
git push origin v.X.Y.Z
```

### CI

**Релиз** (`v.X.Y.Z` → `.github/workflows/build_release.yml`):

1. Тесты, сборка AAB/APK
2. Публикация в Google Play (alpha 50%): **AAB + «Что нового»** из `play/release-notes/` (из CHANGELOG)

Листинг (описания, скриншоты) при релизе **не** обновляется.

**Листинг** (вручную → `.github/workflows/publish_play_listing.yml`):

1. Закоммиченные файлы в `androidApp/src/main/play/listings/`
2. Run workflow **Publish Play listing** в GitHub Actions

Или локально: `.\scripts\publish-play-listing.ps1`.

## Одна команда (только скриншоты)

Gradle (кроссплатформенно):

```powershell
./gradlew :androidApp:buildMarketingScreenshots
```

PowerShell:

```powershell
.\scripts\build-marketing-screenshots.ps1
```

Скрипт:

1. Снимает 7 PNG в `docs/marketing/assets/screenshots/` (локаль RU)
2. Собирает финальные картинки с подписями в `docs/marketing/assets/output/`
3. Копирует в `androidApp/src/main/play/listings/*/graphics/`

Только синхронизация текстов (без скриншотов):

```powershell
python scripts/sync-play-listing-texts.py
```

## По шагам

```powershell
./gradlew :androidApp:recordRoborazziDebug
cd docs/marketing/assets
python scripts/build_assets.py
./gradlew :androidApp:syncPlayListingAssets
```

Тесты: `androidApp/src/test/java/by/tigre/numbers/marketing/MarketingScreenshotTest.kt`  
Фикстуры: `MarketingScreenshotFixtures.kt`

Проверка регрессий (сравнение с эталоном в `androidApp/build/outputs/roborazzi/`):

```powershell
./gradlew :androidApp:verifyRoborazziDebug
```

## Список экранов

| Пункт меню | Файл | Экран |
|------------|------|-------|
| Главное меню | `01-main-menu.png` | Реальное меню, лидерборд включён |
| Игра + таймер | `02-game-timer.png` | 7×8, таймер 01:30 |
| Обратная связь | `03-feedback.png` | «Молодец!», ответ 56 |
| Испытания | `04-challenges.png` | Список с тестовыми данными |
| Лидерборд | `05-leaderboard.png` | Фиксированный топ-5 |
| Статистика | `06-statistics.png` | Демо-данные 7/30 дней |
| Настройки сложности | `07-difficulty.png` | Умножение, выбраны цифры |

## Технически

- Roborazzi + Robolectric, без Firebase и аналитики
- `ScreenshotGameComponent` — статичные данные для экранов игры в тестах
- `MarketingScreenshotFixtures` — фикстуры для остальных экранов
