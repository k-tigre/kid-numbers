# Google Play — креативы Numbers / Числа

Шаблоны для листинга Google Play и UAC-рекламы. Исходники в SVG — можно править в Figma, Inkscape или Illustrator.

## Структура

```
assets/
├── config.json              # тексты, цвета, размеры (редактировать здесь)
├── sources/                 # исходники SVG (генерируются скриптом, можно править вручную)
│   ├── icon.svg             # иконка приложения
│   ├── screenshots/ru|en/   # 8 шаблонов скриншотов 1080×1920
│   ├── feature-graphic/     # Feature Graphic 1024×500
│   └── uac-banners/         # UAC-баннеры 1200×628
├── screenshots/ru|en/       # скриншоты приложения (PNG), по языку
└── output/                  # готовые PNG для загрузки в Play Console
```

## Быстрый старт

### Одна команда — скриншоты + финальные картинки

```powershell
./gradlew :androidApp:buildMarketingScreenshots
```

Или PowerShell: `.\scripts\build-marketing-screenshots.ps1`

Подробнее: [screenshot-build.md](../screenshot-build.md).

### По шагам

1. Снять скриншоты приложения (Roborazzi):

```powershell
./gradlew :androidApp:recordRoborazziDebug
```

2. Собрать креативы:

```powershell
cd docs/marketing/assets
python scripts/build_assets.py
```

Либо положите скриншоты вручную в `screenshots/ru/` и `screenshots/en/`:

| Файл | Экран |
|------|-------|
| `01-main-menu.png` | Главное меню |
| `02-game-timer.png` | Игра + таймер |
| `03-feedback.png` | «Молодец, правильно!» / «Well done, that's right!» |
| `04-challenges.png` | Испытания |
| `05-leaderboard.png` | Лидерборд |
| `06-statistics.png` | Статистика |
| `07-difficulty.png` | Настройки сложности |
| `08-banner.png` | Любой экран (для баннера; опционально) |

Размер сырого скриншота приложения: **1080×1920** (экран телефона 360×640 dp, xxhdpi). Скрипт вставляет его в рамку 868×1548 внутри финального фрейма 1080×1920.

### 3. Пересобрать с реальными скриншотами

```powershell
python scripts/build_assets.py
```

Скриншоты автоматически вставятся в рамку телефона.

### 4. Загрузить в Google Play

**Локально:** `.\scripts\prepare-play-listing.ps1` — тексты и скриншоты, коммит в `master`, затем `.\scripts\publish-play-listing.ps1` или workflow **Publish Play listing**. Release notes при релизе — из [CHANGELOG.md](../../../CHANGELOG.md), загружаются отдельно от листинга.

**Вручную (только файлы):**

| Файл | Куда |
|------|------|
| `output/screenshots/ru/*.png` | Скриншоты (локаль RU) |
| `output/screenshots/en/*.png` | Скриншоты (локаль EN) |
| `output/feature-graphic/feature-graphic-ru.png` | Feature Graphic |
| `output/uac-banners/*.png` | Google Ads UAC |

Тексты листинга: [play-listing.md](../play-listing.md) → `androidApp/src/main/play/listings/` (`sync-play-listing-texts.py`).

## Ручное редактирование

### Изменить тексты

Отредактируйте `config.json` и запустите `python scripts/build_assets.py`.
Либо правьте SVG напрямую в `sources/` — при `--png-only` скрипт не перезапишет их:

```powershell
python scripts/build_assets.py --png-only
```

### Изменить цвета

В `config.json` → секция `brand`:

| Цвет | HEX | Назначение |
|------|-----|------------|
| background | `#FCF8F8` | Фон |
| text | `#494949` | Текст |
| accent | `#F6BE14` | Золотая плашка |
| success | `#4B8200` | Успех |
| error | `#D91B00` | Ошибка |

### Figma

1. Импортируйте SVG из `sources/screenshots/ru/01-main-menu.svg`
2. Замените плейсхолдер на скриншот (слой внутри рамки телефона)
3. Экспортируйте PNG 1080×1920

## Размеры

| Ассет | Размер | Кол-во |
|-------|--------|--------|
| Скриншоты Play | 1080×1920 | 8 × 2 языка |
| Feature Graphic | 1024×500 | 2 (RU/EN) |
| UAC-баннеры | 1200×628 | 4 (2 варианта × 2 языка) |

## Зависимости

```powershell
pip install pillow
```

Опционально `cairosvg` — если установлен, PNG рендерится из SVG. На Windows по умолчанию используется Pillow.
