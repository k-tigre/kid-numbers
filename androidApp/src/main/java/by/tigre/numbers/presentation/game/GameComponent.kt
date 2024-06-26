package by.tigre.numbers.presentation.game

import by.tigre.numbers.domain.GameProvider
import by.tigre.numbers.entity.GameOptions
import by.tigre.numbers.entity.GameResult
import by.tigre.numbers.entity.GameSettings
import by.tigre.tools.logger.extensions.debugLog
import by.tigre.tools.presentation.base.BaseComponentContext
import by.tigre.tools.tools.coroutines.extensions.tickerFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

interface GameComponent {

    val isEnterEnabled: StateFlow<Boolean>
    val question: StateFlow<GameOptions.Question>
    val questionsState: StateFlow<QuestionsState>
    val answer: StateFlow<String>
    val answerResult: StateFlow<Boolean?>
    val timeState: StateFlow<TimeState>

    fun onAnswerChanged(answer: String)
    fun onEnterClicked()
    fun onDoneClicked()
    fun onNextClicked()

    data class QuestionsState(val current: Int, val total: Int, val correctCount: Int)
    data class TimeState(val value: String, val isEnding: Boolean)

    class Impl(
        context: BaseComponentContext,
        settings: GameSettings,
        provider: GameProvider,
        private val onFinish: (GameResult) -> Unit
    ) : GameComponent, BaseComponentContext by context {

        private val gameOption = provider.provide(settings)

        private val resultQuestions = mutableListOf<GameResult.Result>()
        private val allQuestions = gameOption.questions

        private val time = tickerFlow(1000, 0)
            .stateIn(this, SharingStarted.WhileSubscribed(), 0)

        override val answerResult = MutableStateFlow<Boolean?>(null)
        override val answer = MutableStateFlow("")
        override val isEnterEnabled: StateFlow<Boolean> = answer
            .map { it.isNotEmpty() }
            .combine(answerResult) { notEmpty, entered -> notEmpty && entered == null }
            .stateIn(this, SharingStarted.Eagerly, false)

        override val questionsState = MutableStateFlow(QuestionsState(current = 1, total = allQuestions.size, correctCount = 0))
        override val question: StateFlow<GameOptions.Question> = questionsState
            .map { (current, _) -> allQuestions[current - 1] }
            .stateIn(this, SharingStarted.Eagerly, allQuestions[0])

        override val timeState: StateFlow<TimeState> = time
            .map { time ->
                TimeState(
                    TIME_FORMAT.format((gameOption.duration - time).coerceAtLeast(0) * 1000),
                    isEnding = time > gameOption.duration * 0.8
                )
            }
            .stateIn(this, SharingStarted.WhileSubscribed(), TimeState("", false))

        init {
            launch {
                answer
                    .collect {
                        answerResult.emit(null)
                    }
            }

            launch {
                time
                    .filter {
                        it > gameOption.duration
                    }
                    .take(1)
                    .collect {
                        completeQuestions()
                        onNextClicked()
                    }
            }

            launch {
                answerResult
                    .debugLog("answerResult")
                    .debounce(3000)
                    .filter { it != null }
                    .collect {
                        onNextClicked()
                    }
            }
        }

        private fun completeQuestions() {
            val state = questionsState.value
            allQuestions.forEachIndexed { index, question ->
                if (index >= state.current) {
                    val result = GameResult.Result(
                        isCorrect = false,
                        question = question,
                        answer = null
                    )
                    resultQuestions.add(result)
                }
            }
        }

        override fun onAnswerChanged(answer: String) {
            if (answerResult.value == null) {
                this.answer.tryEmit(answer.filter { it.isDigit() }.take(4))
            }
        }

        override fun onNextClicked() {
            val state = questionsState.value
            if (state.total == state.current) {
                onFinish(GameResult(results = resultQuestions, time = TIME_FORMAT.format(time.value * 1000)))
            } else {
                questionsState.tryEmit(
                    state.copy(
                        current = state.current + 1,
                        correctCount = state.correctCount + if (answerResult.value == true) 1 else 0
                    )
                )
                answer.tryEmit("")
            }
        }

        override fun onEnterClicked() {
            val answer = answer.value.toIntOrNull() ?: return
            val question = question.value
            val result = GameResult.Result(
                isCorrect = answer == question.result,
                question = question,
                answer = answer
            )
            resultQuestions.add(result)
            answerResult.tryEmit(result.isCorrect)
        }

        override fun onDoneClicked() {
            if (answerResult.value == null) {
                onEnterClicked()
            } else {
                onNextClicked()
            }
        }

        private companion object {
            val TIME_FORMAT = SimpleDateFormat("mm:ss", Locale.US)
        }
    }
}
