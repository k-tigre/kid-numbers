package by.tigre.numbers.presentation.settings

import by.tigre.numbers.data.storage.LeaderboardPreferences
import by.tigre.tools.presentation.base.BaseComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface SettingsComponent {
    val uiState: StateFlow<SettingsUiState>
    fun onNicknameChanged(value: String)
    fun onSaveClicked()
    fun onCloseClicked()
}

data class SettingsUiState(
    val nickname: String = "",
    val error: SettingsError? = null,
    val saved: Boolean = false,
) {
    enum class SettingsError {
        EmptyNickname,
    }
}

class SettingsComponentImpl(
    context: BaseComponentContext,
    private val leaderboardPreferences: LeaderboardPreferences,
    private val onClose: () -> Unit,
) : SettingsComponent, BaseComponentContext by context {

    private val _uiState: MutableStateFlow<SettingsUiState> = MutableStateFlow(
        SettingsUiState(nickname = leaderboardPreferences.loadNickname(default = DEFAULT_NICKNAME)),
    )
    override val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    override fun onNicknameChanged(value: String) {
        _uiState.value = _uiState.value.copy(nickname = value.take(NICKNAME_MAX_LENGTH), error = null, saved = false)
    }

    override fun onSaveClicked() {
        val trimmed: String = _uiState.value.nickname.trim()
        if (trimmed.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = SettingsUiState.SettingsError.EmptyNickname)
            return
        }
        leaderboardPreferences.saveNickname(trimmed)
        _uiState.value = _uiState.value.copy(nickname = trimmed, error = null, saved = true)
    }

    override fun onCloseClicked() = onClose()

    private companion object {
        const val DEFAULT_NICKNAME: String = "Player"
        const val NICKNAME_MAX_LENGTH: Int = 24
    }
}
