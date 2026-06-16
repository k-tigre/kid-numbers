# Architecture

## Pattern: Decompose Component + Compose View

No MVVM — there are no `ViewModel` classes. A screen = `XxxComponent` + `XxxView`.

```
presentation/menu/
  MenuComponent.kt    # interface + class Impl
  MenuView.kt         # ComposableView or ScreenComposableView
```

**Component**
- Public API: state (`StateFlow`) + callbacks.
- `Impl` implements `BaseComponentContext` (Decompose `ComponentContext` + `CoroutineScope`).
- Async lists may use `ScreenContentState` via `ScreenContentStateDelegate`.

**View**
- Implements `ComposableView` or `ScreenComposableView`.
- `@Composable fun Draw(modifier: Modifier)`.
- Collects component state with `collectAsState()`.

**Navigation** — `RootComponent` (`presentation/root/`): `ChildStack` for screens, `ChildSlot` for dialogs. Register new screens here.

## Layers

| Layer | Package | Purpose |
|-------|---------|---------|
| UI | `presentation/` | Components, Views, navigation |
| Domain | `domain/` | `GameProvider`, `GameDurationProvider`, reminders |
| Data | `data/` | SQLDelight, Firestore, Remote Config, preferences |
| Models | `entity/` | `GameType`, `GameSettings`, `Challenge`, `GameResult`, … |
| DI | `di/` | `ApplicationGraph`, `*Module` classes |

Manual DI — no Hilt/Koin. Wire new dependencies through the appropriate `*Module` and `ApplicationGraph`.

## Shared modules (`tools/`)

| Module | Key types |
|--------|-----------|
| `presentation/compose` | `ComposableView`, `ScreenComposableView`, theme |
| `presentation/decompose` | `BaseComponentContext`, `ScreenContentStateDelegate` |
| `coroutines` | `CoreDispatchers`, `CoroutineModule` |

## Adding a screen

```kotlin
// 1. Component
interface MyScreenComponent {
    val title: StateFlow<String>
    fun onBackClicked()
    class Impl(context: BaseComponentContext, …) : MyScreenComponent, BaseComponentContext by context { … }
}

// 2. View
class MyScreenView(private val component: MyScreenComponent) : ComposableView {
    @Composable
    override fun Draw(modifier: Modifier) { … }
}

// 3. RootComponent — PageChild, Config, navigation
// 4. di/ — inject dependencies if needed
```
