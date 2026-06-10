package by.tigre.numbers.presentation.leaderboard

import by.tigre.numbers.data.leaderboard.LeaderboardHistoryBackfill
import by.tigre.numbers.data.leaderboard.LeaderboardRepository
import by.tigre.numbers.data.storage.LeaderboardPreferences
import by.tigre.numbers.entity.GameSettings
import by.tigre.numbers.entity.LeaderboardEntry
import by.tigre.numbers.entity.LeaderboardSpeedEntry
import by.tigre.tools.presentation.base.BaseComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface LeaderboardComponent {
    val uiState: StateFlow<LeaderboardUiState>
    fun onBack()
    fun onRefresh()
    fun onTabSelected(tab: LeaderboardTab)
}

enum class LeaderboardTab {
    Speed,
    Total,
}

data class LeaderboardUiState(
    val selectedTab: LeaderboardTab = LeaderboardTab.Speed,
    val speedBoardSettings: GameSettings? = null,
    val speedEntries: List<LeaderboardSpeedEntry> = emptyList(),
    val totalEntries: List<LeaderboardEntry> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val backfillSubmittedCount: Int? = null,
)

class LeaderboardComponentImpl(
    context: BaseComponentContext,
    private val leaderboardRepository: LeaderboardRepository,
    private val leaderboardHistoryBackfill: LeaderboardHistoryBackfill,
    private val leaderboardPreferences: LeaderboardPreferences,
    private val navigateBack: () -> Unit,
) : LeaderboardComponent, BaseComponentContext by context {

    private val _uiState: MutableStateFlow<LeaderboardUiState> = MutableStateFlow(LeaderboardUiState())
    override val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init {
        launch {
            leaderboardHistoryBackfill.runIfNeeded(defaultNickname = DEFAULT_NICKNAME)
                .onSuccess { count ->
                    if (count > 0) {
                        _uiState.value = _uiState.value.copy(backfillSubmittedCount = count)
                    }
                }
            loadEntries()
        }
    }

    override fun onBack() = navigateBack()

    override fun onRefresh() = loadEntries()

    override fun onTabSelected(tab: LeaderboardTab) {
        if (tab == _uiState.value.selectedTab) return
        _uiState.value = _uiState.value.copy(selectedTab = tab)
        loadEntries()
    }

    private fun loadEntries() {
        val tab: LeaderboardTab = _uiState.value.selectedTab
        launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (tab) {
                LeaderboardTab.Speed -> {
                    val settings: GameSettings? = leaderboardPreferences.loadLastBoardSettings()
                    val boardKey: String? = leaderboardPreferences.loadLastBoardKey()
                    if (settings == null || boardKey == null) {
                        _uiState.value = _uiState.value.copy(
                            speedBoardSettings = null,
                            speedEntries = emptyList(),
                            isLoading = false,
                        )
                        return@launch
                    }
                    leaderboardRepository.fetchTopSpeedEntries(boardKey = boardKey, limit = 50)
                        .onSuccess { entries ->
                            _uiState.value = _uiState.value.copy(
                                speedBoardSettings = settings,
                                speedEntries = entries,
                                isLoading = false,
                            )
                        }
                        .onFailure { error ->
                            _uiState.value = _uiState.value.copy(
                                speedBoardSettings = settings,
                                isLoading = false,
                                errorMessage = error.message,
                            )
                        }
                }
                LeaderboardTab.Total -> {
                    leaderboardRepository.fetchTopTotalEntries(limit = 50)
                        .onSuccess { entries ->
                            _uiState.value = _uiState.value.copy(
                                totalEntries = entries,
                                isLoading = false,
                            )
                        }
                        .onFailure { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = error.message,
                            )
                        }
                }
            }
        }
    }

    private companion object {
        const val DEFAULT_NICKNAME: String = "Player"
    }
}
