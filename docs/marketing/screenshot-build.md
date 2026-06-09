# Screenshot build



Съёмка скриншотов Google Play без эмулятора — через Roborazzi (JVM + Robolectric).



## CI / релиз

При пуше тега `v.X.Y.Z` workflow `.github/workflows/build_release.yml`:

1. Берёт версию из тега и обновляет `Application.kt`
2. Читает «Что нового» из `CHANGELOG.md` (секции `### RU` / `### EN`)
3. Снимает скриншоты Roborazzi и собирает креативы
4. Копирует PNG в `androidApp/src/main/play/listings/` для Gradle Play Publisher
5. Загружает в Google Play (alpha 50%): описания, скриншоты, feature graphic, release notes, AAB
6. Коммитит в `master`: версию, CHANGELOG, `play/listings/`, `play/release-notes/`, `docs/marketing/assets/output/`

### Подготовка релиза

1. Заполните секцию `## [X.Y.Z]` в [CHANGELOG.md](../../CHANGELOG.md):

```markdown
## [1.2.3]

### RU
- Исправлен баг …

### EN
- Fixed bug …
```

2. Создайте и запушьте тег:

```powershell
git tag v.1.2.3
git push origin v.1.2.3
```

Локально (без публикации в Play):

```powershell
$env:RELEASE_VERSION = "1.2.3"
./scripts/bump-version.sh
./scripts/generate-play-release-notes.sh alpha
./gradlew :androidApp:buildMarketingScreenshots
```

Тексты листинга: `androidApp/src/main/play/listings/{ru-RU,en-US}/`.

## Одна команда



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



## По шагам



```powershell

./gradlew :androidApp:recordRoborazziDebug

cd docs/marketing/assets

python scripts/build_assets.py

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


