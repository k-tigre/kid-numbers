---
name: prepare-play-listing
description: >-
  Updates Google Play listing texts from docs, rebuilds marketing screenshots,
  and publishes listing to Play (separate from app releases). Use when the user
  asks to update Play store description, listing texts, screenshots, feature
  graphic, or says «обнови листинг», «пересобери скриншоты для Play»,
  «залей листинг в Play».
---

# Prepare Play Listing

Обновление описаний и скриншотов Google Play — **отдельно** от релиза приложения.

## Что делает

1. Копирует тексты из [docs/marketing/play-listing.md](../../docs/marketing/play-listing.md) в `androidApp/src/main/play/listings/`
2. Снимает скриншоты Roborazzi и собирает креативы (`recordMarketingScreenshots`)
3. Копирует PNG в `play/listings/*/graphics/` и `docs/marketing/assets/output/`

## Когда запускать

- Изменились тексты листинга (название, краткое/полное описание)
- Изменился UI — нужны новые скриншоты
- Обновили подписи в [docs/marketing/assets/config.json](../../docs/marketing/assets/config.json)

**Не нужно** при каждом patch-релизе — только когда меняется маркетинг.

## Шаг 1. Отредактируйте тексты (если нужно)

Файл: [docs/marketing/play-listing.md](../../docs/marketing/play-listing.md)

Секции `## ru-RU` / `## en-US`, поля `### title`, `### short-description`, `### full-description`.

Лимиты Google Play: title 30, short 80, full 4000 символов.

Подписи на скриншотах: [docs/marketing/assets/config.json](../../docs/marketing/assets/config.json) → `screenshots[].captionRu` / `captionEn`.

## Шаг 2. Пересоберите ассеты

```powershell
.\scripts\prepare-play-listing.ps1
```

Linux / Git Bash:

```bash
chmod +x scripts/prepare-play-listing.sh
./scripts/prepare-play-listing.sh
```

Только тексты (без пересъёмки скриншотов):

```powershell
python scripts/sync-play-listing-texts.py
```

Только скриншоты (тексты уже актуальны):

```powershell
./gradlew :androidApp:buildMarketingScreenshots
```

## Шаг 3. Проверка

```powershell
python scripts/sync-play-listing-texts.py   # dry-run: сравните diff, если меняли md
git diff androidApp/src/main/play/listings/ docs/marketing/assets/output/
```

Откройте несколько PNG в `docs/marketing/assets/output/screenshots/ru/`.

## Шаг 4. Закоммитьте

```powershell
git add docs/marketing/play-listing.md
git add androidApp/src/main/play/listings/
git add docs/marketing/assets/output/
git add docs/marketing/assets/config.json   # если меняли подписи
git commit -m "Update Play listing texts and screenshots"
git push origin master
```

## Шаг 5. Опубликуйте листинг в Play

Отдельно от релиза приложения. Релизный CI **не** трогает описания и скриншоты.

**GitHub Actions** (рекомендуется): Actions → **Publish Play listing** → Run workflow.

**Локально** (нужны `ANDROID_PUBLISHER_CREDENTIALS`, `PLAY_CONTACT_EMAIL`):

```powershell
.\scripts\publish-play-listing.ps1
```

## Зависимости (локально)

```powershell
pip install pillow
```

Подробнее о скриншотах: [docs/marketing/screenshot-build.md](../../docs/marketing/screenshot-build.md).

## Связанные файлы

- [docs/marketing/play-listing.md](../../docs/marketing/play-listing.md) — тексты листинга
- [scripts/sync-play-listing-texts.py](../../scripts/sync-play-listing-texts.py)
- [scripts/prepare-play-listing.ps1](../../scripts/prepare-play-listing.ps1)
- [scripts/publish-play-listing.ps1](../../scripts/publish-play-listing.ps1)
- [.github/workflows/publish_play_listing.yml](../../.github/workflows/publish_play_listing.yml)
- [androidApp/build.gradle.kts](../../androidApp/build.gradle.kts) — `publishPlayListing`, `buildMarketingScreenshots`
