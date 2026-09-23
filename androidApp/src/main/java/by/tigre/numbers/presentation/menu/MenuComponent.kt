package by.tigre.numbers.presentation.menu

import by.tigre.numbers.data.facts.FactStore
import by.tigre.numbers.data.remoteconfig.FeatureFlags
import by.tigre.numbers.di.ChallengesDependencies
import by.tigre.numbers.entity.GameType
import by.tigre.tools.presentation.base.BaseComponentContext
import by.tigre.tools.tools.coroutines.CoreDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

interface MenuComponent {
    val gameTypes: List<GameType>
    val hasActiveChallenge: StateFlow<Boolean>
    val isLeaderboardEnabled: StateFlow<Boolean>
    val isSmartPracticeEnabled: StateFlow<Boolean>
    val smartPracticeDueCount: StateFlow<Int>

    fun onGameClicked(type: GameType)
    fun onSmartPracticeClicked()
    fun onHistoryClicked()
    fun onChallengeClicked()
    fun onStatisticClicked()
    fun onLeaderboardClicked()
    fun onSettingsClicked()

    interface Router {
        fun showGameSettings(type: GameType)
        fun showSmartPractice()
        fun showHistory()
        fun showChallenge()
        fun showStatistic()
        fun showLeaderboard()
        fun showSettings()
    }

    class Impl(
        context: BaseComponentContext,
        private val router: Router,
        challengesDependencies: ChallengesDependencies,
        featureFlags: FeatureFlags,
        factStore: FactStore,
        dispatchers: CoreDispatchers,
    ) : MenuComponent, BaseComponentContext by context {

        override val hasActiveChallenge: StateFlow<Boolean> = challengesDependencies.challengesStore.hasActiveChallenge
            .stateIn(this, started = SharingStarted.WhileSubscribed(), initialValue = false)
        override val isLeaderboardEnabled: StateFlow<Boolean> = featureFlags.isLeaderboardEnabled
        override val isSmartPracticeEnabled: StateFlow<Boolean> = featureFlags.isSmartPracticeEnabled

        @OptIn(ExperimentalCoroutinesApi::class)
        override val smartPracticeDueCount: StateFlow<Int> = featureFlags.isSmartPracticeEnabled
            .flatMapLatest { enabled ->
                if (!enabled) {
                    flowOf(0)
                } else {
                    flow {
                        val count: Int = withContext(dispatchers.io) {
                            factStore.countDue(featureFlags.smartOpsEnabled(), System.currentTimeMillis())
                        }
                        emit(count)
                    }
                }
            }
            .stateIn(this, started = SharingStarted.WhileSubscribed(), initialValue = 0)

        override val gameTypes: List<GameType> = GameType.entries

        override fun onGameClicked(type: GameType) {
            router.showGameSettings(type)
        }

        override fun onSmartPracticeClicked() {
            router.showSmartPractice()
        }

        override fun onHistoryClicked() {
            router.showHistory()
        }

        override fun onChallengeClicked() {
            router.showChallenge()
        }

        override fun onStatisticClicked() {
            router.showStatistic()
        }

        override fun onLeaderboardClicked() {
            router.showLeaderboard()
        }

        override fun onSettingsClicked() {
            router.showSettings()
        }
    }
}
