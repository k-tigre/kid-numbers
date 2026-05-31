package by.tigre.numbers.presentation.leaderboard

import by.tigre.numbers.data.leaderboard.LeaderboardHistoryBackfill
import by.tigre.numbers.data.leaderboard.LeaderboardRepository
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.LeaderboardEntry
import by.tigre.tools.presentation.base.BaseComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface LeaderboardComponent {
    val uiState: StateFlow<LeaderboardUiState>
    fun onBack()
    fun onRefresh()
    fun onDifficultSelected(difficult: Difficult)
}

data class LeaderboardUiState(
    val selectedDifficult: Difficult = Difficult.Easy,
    val entries: List<LeaderboardEntry> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val backfillSubmittedCount: Int? = null,
)

class LeaderboardComponentImpl(
    context: BaseComponentContext,
    private val leaderboardRepository: LeaderboardRepository,
    private val leaderboardHistoryBackfill: LeaderboardHistoryBackfill,
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

    override fun onDifficultSelected(difficult: Difficult) {
        if (difficult == _uiState.value.selectedDifficult) return
        _uiState.value = _uiState.value.copy(selectedDifficult = difficult)
        loadEntries()
    }

    private fun loadEntries() {
        val difficult: Difficult = _uiState.value.selectedDifficult
        launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            leaderboardRepository.fetchTopEntries(difficult = difficult, limit = 50)
                .onSuccess { entries ->
                    _uiState.value = _uiState.value.copy(
                        entries = entries,
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

    private companion object {
        const val DEFAULT_NICKNAME: String = "Player"
    }
}
