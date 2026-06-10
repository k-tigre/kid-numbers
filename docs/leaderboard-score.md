# Рейтинг (score) для доски почёта Numbers

## Две доски

### 1. Скорость (`leaderboard_speed`)

Лучшее время на **точной конфигурации** задания. Сравнение честное: тип задачи, сложность, диапазон, таблица умножения, тип уравнений.

Ключ доски (`boardKey`) — строка из настроек, например `Mult_2-5-10_Easy`.

Документ: `{boardKey}_{userId}`.

Поля: `bestTimeSeconds`, `mistakes`, `taskCount`, `nickname`, `timestamp`.

Топ: `boardKey` + `bestTimeSeconds` по возрастанию.

Обновление: только если новое время лучше (или равно, но меньше ошибок).

### 2. Общий счёт (`leaderboard_total`)

Накопительный рейтинг: один документ на игрока (`userId`). Чем больше идеальных тренировок — тем выше счёт.

## Очки за одну игру (общий счёт)

```
timeCapSeconds = лимит времени для настроек (GameDurationProvider)
elapsedSeconds = фактическое время игры (секунды)
mistakes       = число неверных ответов

gameScore = timeCapSeconds − elapsedSeconds − mistakePenalty × mistakes − hintPenalty × hintsUsed
gameScore = max(minRating, gameScore)
```

### Коэффициенты (Remote Config `leaderboard_rating_json`)

```json
{"hintPenalty":0,"mistakePenalty":10,"minRating":1}
```

После смены в Console обновите константы в `docs/leaderboard-firestore-rules.txt`.

## Накопление (общий счёт)

```
totalScore = totalScore + gameScore
gamesCount = gamesCount + 1
```

**userId** — UUID, генерируется один раз в приложении. **nickname** — только для отображения.

## Сравнение после игры

После идеальной тренировки приложение запрашивает лучшее время на той же `boardKey` и показывает:

- «Первый рекорд на этой доске!»
- «Ты быстрее лучшего на X.X с»
- «До рекорда осталось X.X с»
- «Ты повторил рекорд!»

## Когда начислять

- Только **идеальные** тренировки: `correctCount == totalCount`.
- Испытания (challenges) не отправляются.

## Первичное наполнение из истории

При первом открытии доски почёта идеальные записи из истории отправляются только в **общий счёт** (в истории нет полных настроек для speed-доски).

## Поля Firestore

### `leaderboard_speed`

| Поле | Назначение |
|------|------------|
| `userId` | UUID игрока |
| `nickname` | Отображаемое имя |
| `boardKey` | Ключ конфигурации |
| `gameType` | Тип задачи |
| `difficulty` | Easy / Medium / Hard |
| `taskCount` | Число задач в сессии |
| `bestTimeSeconds` | Лучшее время |
| `solveTimeSeconds` | Время последней попытки (для Rules) |
| `mistakes` | Ошибки лучшего результата |
| `timestamp` | Время обновления |

### `leaderboard_total`

| Поле | Назначение |
|------|------------|
| `userId` | UUID игрока |
| `nickname` | Отображаемое имя |
| `score` | Суммарный рейтинг |
| `gamesCount` | Число учтённых идеальных игр |
| `timeCapSeconds` | Лимит последней игры (для Rules) |
| `solveTimeSeconds` | Время последней игры |
| `mistakes` | Ошибки последней игры |
| `hintsUsed` | `0` |
| `timestamp` | Время последнего обновления |
