package by.tigre.numbers.presentation.menu

import by.tigre.numbers.di.ChallengesDependencies
import by.tigre.numbers.entity.GameType
import by.tigre.tools.presentation.base.BaseComponentContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

interface MenuComponent {
    val gameTypes: List<GameType>
    val hasActiveChallenge: StateFlow<Boolean>

    fun onGameClicked(type: GameType)
    fun onHistoryClicked()
    fun onChallengeClicked()

    interface Router {
        fun showGameSettings(type: GameType)
        fun showHistory()
        fun showChallenge()
    }

    class Impl(
        context: BaseComponentContext,
        private val router: Router,
        challengesDependencies: ChallengesDependencies
    ) : MenuComponent, BaseComponentContext by context {

        override val hasActiveChallenge: StateFlow<Boolean> = challengesDependencies.challengesStore.hasActiveChallenge
            .stateIn(this, started = SharingStarted.WhileSubscribed(), initialValue = false)

        override val gameTypes: List<GameType> = GameType.entries

        override fun onGameClicked(type: GameType) {
            router.showGameSettings(type)
        }

        override fun onHistoryClicked() {
            router.showHistory()
        }

        override fun onChallengeClicked() {
            router.showChallenge()
        }
    }
}
