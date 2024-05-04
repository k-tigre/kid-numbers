package by.tigre.numbers.presentation.multiplication.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.tigre.numbers.presentation.multiplication.component.MultiplicationGameComponent
import by.tigre.numbers.presentation.multiplication.component.MultiplicationGameComponent.TimeState
import by.tigre.tools.tools.platform.compose.AppTheme
import by.tigre.tools.tools.platform.compose.ComposableView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MultiplicationGameView(
    private val component: MultiplicationGameComponent,
) : ComposableView {

    @Composable
    override fun Draw(modifier: Modifier) {
        Column(modifier) {
            DrawTime()
            DrawTitle()
            DrawQuestion()
            DrawAnswer()
            DrawAnswerButton()
            DrawResultButton()
        }
    }

    @Composable
    private fun ColumnScope.DrawTime() {
        val state = component.timeState.collectAsState().value
        Text(
            modifier = Modifier
                .align(Alignment.End)
                .padding(horizontal = 32.dp, vertical = 8.dp),
            text = "Осталось ${state.value}",
            color = if (state.isEnding) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            style = if (state.isEnding) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleSmall,
        )
    }

    @Composable
    private fun ColumnScope.DrawTitle() {
        val state = component.questionsState.collectAsState().value
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp),
            text = "Вопрос ${state.current} из ${state.total}"
        )

        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 32.dp),
            text = "Дано равильных ответов: ${state.correctCount}"
        )
    }

    @Composable
    private fun ColumnScope.DrawQuestion() {
        val question = component.question.collectAsState().value
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(32.dp),
            text = "${question.first} * ${question.second} = ?",
            style = MaterialTheme.typography.titleLarge,
        )
    }

    @Composable
    private fun ColumnScope.DrawAnswer() {
        val answer = component.answer.collectAsState()

        TextField(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp),
            value = answer.value,
            onValueChange = component::onAnswerChanged,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { component.onDoneClicked() },
            ),
            label = { Text("Ответ") }
        )
    }

    @Composable
    private fun ColumnScope.DrawAnswerButton() {
        Button(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp),
            onClick = component::onEnterClicked,
            enabled = component.isEnterEnabled.collectAsState().value
        ) {
            Text(text = "Ответить")
        }
    }

    @Composable
    private fun ColumnScope.DrawResultButton() {
        val result = component.answerResult.collectAsState().value

        AnimatedVisibility(
            visible = result != null,
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(500)),
        ) {
            if (result != null) {
                Column(modifier = Modifier.fillMaxWidth()) {

                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(32.dp),
                        text = if (result) "Молодец, правильно!" else "Неправильно, попробуй еще",
                        color = if (result) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleLarge,
                    )

                    Button(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp),
                        onClick = component::onNextClicked,
                    ) {
                        Text(text = "Следующий вопрос")
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    val component = object : MultiplicationGameComponent {
        override val isEnterEnabled: StateFlow<Boolean> = MutableStateFlow(true)
        override val question: StateFlow<MultiplicationGameComponent.Question> =
            MutableStateFlow(MultiplicationGameComponent.Question(1, 3, 4))
        override val questionsState: StateFlow<MultiplicationGameComponent.QuestionsState> =
            MutableStateFlow(MultiplicationGameComponent.QuestionsState(3, 1, 12))
        override val answer: StateFlow<String> = MutableStateFlow("12")
        override val answerResult: StateFlow<Boolean?> = MutableStateFlow(true)
        override val timeState: StateFlow<TimeState> = MutableStateFlow(TimeState("19:19", true))

        override fun onAnswerChanged(answer: String) = Unit
        override fun onEnterClicked() = Unit
        override fun onNextClicked() = Unit
        override fun onDoneClicked() = Unit
    }

    AppTheme {
        Surface(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
        ) {
            MultiplicationGameView(
                component = component,
            ).Draw(Modifier)
        }
    }
}