package by.tigre.numbers.presentation.challenge.creator

import by.tigre.numbers.analytics.Event
import by.tigre.numbers.analytics.EventAnalytics
import by.tigre.numbers.analytics.ScreenAnalytics
import by.tigre.numbers.data.challenges.ChallengesStore
import by.tigre.numbers.di.ChallengesDependencies
import by.tigre.numbers.domain.GameDurationProvider
import by.tigre.numbers.entity.Challenge
import by.tigre.numbers.entity.GameSettings
import by.tigre.numbers.entity.GameType
import by.tigre.numbers.presentation.game.settings.AdditionalSettingsComponent
import by.tigre.numbers.presentation.game.settings.EquationsSettingsComponent
import by.tigre.numbers.presentation.game.settings.GameSettingsComponentProvider
import by.tigre.numbers.presentation.game.settings.MultiplicationSettingsComponent
import by.tigre.tools.presentation.base.BaseComponentContext
import by.tigre.tools.presentation.base.appChildSlot
import by.tigre.tools.presentation.base.appChildStack
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.popTo
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface DetailsComponent {

    val pages: Value<ChildStack<*, PageChild>>
    val dialogs: Value<ChildSlot<*, DialogChild>>
    val tasks: StateFlow<List<Challenge.Task>>
    val durations: StateFlow<Durations>
    val addDialogVisible: StateFlow<Boolean>
    val mode: StateFlow<Mode>

    fun onCloseClicked()
    fun onSaveClicked()
    fun onAddClicked()
    fun onRemoveClicked(task: Challenge.Task)
    fun onRemoveChallengeClicked()
    fun onDismissDialog()
    fun onEditClicked()
    fun onTaskTypeSelected(type: GameType)
    fun onChallengeDurationSelected(duration: Challenge.Duration)

    sealed interface PageChild {
        data object TaskList : PageChild
        class SettingsMultiplication(val component: MultiplicationSettingsComponent) : PageChild
        class SettingsAdditional(val component: AdditionalSettingsComponent) : PageChild
        class SettingsEquations(val component: EquationsSettingsComponent) : PageChild
    }

    sealed interface DialogChild {
        data object TaskType : DialogChild
        data object ChallengeDuration : DialogChild
        data object ConfirmRemove : DialogChild
    }

    data class Durations(val selected: Challenge.Duration?, val total: Long)

    enum class Mode {
        ViewEditable, View, Edit, Creation
    }

    class Impl(
        context: BaseComponentContext,
        screenAnalytics: ScreenAnalytics,
        analytics: EventAnalytics,
        dependencies: ChallengesDependencies,
        private val challengeId: String?,
        private val onClose: () -> Unit,
    ) : DetailsComponent, BaseComponentContext by context {

        private val store: ChallengesStore = dependencies.challengesStore
        private val gameDurationProvider: GameDurationProvider = dependencies.getDurationProvider()

        private val dialogsNavigation = SlotNavigation<DialogsConfig>()

        override val tasks = MutableStateFlow(emptyList<Challenge.Task>())
        override val addDialogVisible = MutableStateFlow(false)

        private val pagesNavigation = StackNavigation<PagesConfig>()
        private val settingsComponentProvider = GameSettingsComponentProvider.Impl(
            analytics = analytics,
            onClose = onClose,
            onConfirmSettings = ::onConfirmSettings,
            dispatchers = dependencies.dispatchers
        )

        private fun onConfirmSettings(settings: GameSettings) {
            tasks.tryEmit(tasks.value + Challenge.Task(id = -1, gameSettings = settings, isCompleted = false))
            pagesNavigation.popTo(0)
        }

        override val mode = MutableStateFlow(if (challengeId != null) Mode.ViewEditable else Mode.Creation)
        override val durations = MutableStateFlow(Durations(null, 0))

        override val pages: Value<ChildStack<*, PageChild>> =
            appChildStack(
                source = pagesNavigation,
                initialStack = { listOf(PagesConfig.TaskList) },
                serializer = PagesConfig.serializer(),
                key = "pages_challenge_creator",
                handleBackButton = true
            ) { config, componentContext ->
                when (config) {
                    is PagesConfig.TaskList -> PageChild.TaskList

                    is PagesConfig.SettingsMultiplication -> PageChild.SettingsMultiplication(
                        settingsComponentProvider.createMultiplicationSettingsComponent(
                            context = componentContext,
                            isPositive = config.isPositive,
                        )
                    )

                    is PagesConfig.SettingsAdditional -> PageChild.SettingsAdditional(
                        settingsComponentProvider.createAdditionalSettingsComponent(
                            context = componentContext,
                            isPositive = config.isPositive,
                        )
                    )

                    is PagesConfig.SettingsEquations -> PageChild.SettingsEquations(
                        settingsComponentProvider.createEquationsSettingsComponent(
                            context = componentContext,
                        )
                    )
                }
            }

        override val dialogs: Value<ChildSlot<*, DialogChild>> =
            appChildSlot(
                source = dialogsNavigation,
                serializer = DialogsConfig.serializer(),
                key = "challenge_dialogs",
                handleBackButton = true,
            ) { config, _ ->
                when (config) {
                    DialogsConfig.ChallengeDuration -> DialogChild.ChallengeDuration
                    DialogsConfig.ConfirmRemove -> DialogChild.ConfirmRemove
                    DialogsConfig.TaskType -> DialogChild.TaskType
                }
            }

        init {
            if (challengeId != null) {
                launch {
                    val challenge = store.getChallenge(challengeId)

                    if (challenge != null) {
                        tasks.emit(challenge.tasks)
                        durations.emit(
                            Durations(
                                selected = challenge.duration,
                                total = -1
                            )
                        )
                        if (challenge.startDate > 0) {
                            mode.emit(Mode.View)
                        }
                    } else {
                        store.remove(id = challengeId)
                        analytics.trackEvent(Event.Action.Logic.WrongChallengeInDB)
                        onClose()
                    }
                }
            } else {
                launch {
                    dialogsNavigation.activate(DialogsConfig.ChallengeDuration)
                }
            }

            launch {
                tasks
                    .map { it.sumOf { gameDurationProvider.provide(it.gameSettings) } }
                    .distinctUntilChanged()
                    .collect {
                        durations.emit(durations.value.copy(total = it))
                    }

            }
        }

        override fun onEditClicked() {
            mode.tryEmit(Mode.Edit)
        }

        override fun onRemoveChallengeClicked() {
            launch {
                challengeId?.let { store.remove(it) }
                onClose()
            }
            dialogsNavigation.dismiss()
        }

        override fun onCloseClicked() = onClose()
        override fun onSaveClicked() {
            launch {
                val currentTasks = tasks.value
                if (currentTasks.isNotEmpty()) {
                    store.add(
                        Challenge(
                            id = challengeId ?: Challenge.NO_ID,
                            tasks = tasks.value,
                            duration = durations.value.selected ?: Challenge.Duration.TenMinutes,
                            status = Challenge.Status.New,
                            startDate = -1,
                            endDate = -1,
                            isSuccess = false
                        )
                    )

                    if (challengeId != null) {
                        mode.tryEmit(Mode.ViewEditable)
                    } else {
                        onClose()
                    }
                } else {
                    if (challengeId != null) {
                        dialogsNavigation.activate(DialogsConfig.ConfirmRemove)
                    } else {
                        onClose()
                    }
                }
            }
        }

        override fun onAddClicked() {
            dialogsNavigation.activate(DialogsConfig.TaskType)
        }

        override fun onRemoveClicked(task: Challenge.Task) {
            tasks.tryEmit(tasks.value - task)
        }

        override fun onTaskTypeSelected(type: GameType) {
            pagesNavigation.pushNew(
                when (type) {
                    GameType.Additional -> PagesConfig.SettingsAdditional(isPositive = true)
                    GameType.Subtraction -> PagesConfig.SettingsAdditional(isPositive = false)
                    GameType.Multiplication -> PagesConfig.SettingsMultiplication(isPositive = true)
                    GameType.Division -> PagesConfig.SettingsMultiplication(isPositive = false)
                    GameType.Equations -> PagesConfig.SettingsEquations
                }
            )
            dialogsNavigation.dismiss()
        }

        override fun onDismissDialog() {
            dialogsNavigation.dismiss()
        }

        override fun onChallengeDurationSelected(duration: Challenge.Duration) {
            dialogsNavigation.dismiss()
            durations.tryEmit(durations.value.copy(selected = duration))
        }

        @Serializable
        private sealed interface PagesConfig {
            @Serializable
            @SerialName("List")
            data object TaskList : PagesConfig

            @Serializable
            @SerialName("SettingsAdditional")
            data class SettingsAdditional(val isPositive: Boolean) : PagesConfig

            @Serializable
            @SerialName("SettingsEquations")
            data object SettingsEquations : PagesConfig

            @Serializable
            @SerialName("SettingsMultiplication")
            data class SettingsMultiplication(val isPositive: Boolean) : PagesConfig
        }

        @Serializable
        private sealed interface DialogsConfig {
            @Serializable
            @SerialName("TaskType")
            data object TaskType : DialogsConfig

            @Serializable
            @SerialName("ChallengeDuration")
            data object ChallengeDuration : DialogsConfig

            @Serializable
            @SerialName("ConfirmRemove")
            data object ConfirmRemove : DialogsConfig
        }
    }
}
