package by.tigre.numbers.presentation.screenshot

import by.tigre.numbers.entity.GameOptions
import by.tigre.numbers.presentation.game.GameComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class ScreenshotGameMode {
    Timer,
    Feedback,
}

class ScreenshotGameComponent(
    mode: ScreenshotGameMode,
) : GameComponent {
    private val questionValue: GameOptions.Question.Operation.Multiplication =
        GameOptions.Question.Operation.Multiplication(first = 7, second = 8)
    override val isEnterEnabled: StateFlow<Boolean> = MutableStateFlow(mode == ScreenshotGameMode.Timer)
    override val question: StateFlow<GameOptions.Question> = MutableStateFlow(questionValue)
    override val questionsState: StateFlow<GameComponent.QuestionsState> = MutableStateFlow(
        GameComponent.QuestionsState(current = 3, total = 10, correctCount = 2)
    )
    override val answerX: StateFlow<String> = MutableStateFlow(if (mode == ScreenshotGameMode.Feedback) "56" else "")
    override val answerY: StateFlow<String> = MutableStateFlow("")
    override val answerResult: StateFlow<Boolean?> = MutableStateFlow(
        if (mode == ScreenshotGameMode.Feedback) true else null
    )
    override val timeState: StateFlow<GameComponent.TimeState> = MutableStateFlow(
        GameComponent.TimeState(value = if (mode == ScreenshotGameMode.Feedback) "02:45" else "01:30", isEnding = false)
    )
    override fun onAnswerChanged(answer: String) = Unit
    override fun onEnterClicked() = Unit
    override fun onDoneClicked() = Unit
    override fun onNextClicked() = Unit
}
