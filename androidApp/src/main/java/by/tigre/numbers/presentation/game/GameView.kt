package by.tigre.numbers.presentation.game

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.tigre.numbers.R
import by.tigre.numbers.entity.GameOptions
import by.tigre.numbers.presentation.game.GameComponent.TimeState
import by.tigre.tools.tools.platform.compose.AppTheme
import by.tigre.tools.tools.platform.compose.ComposableView
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GameView(
    private val component: GameComponent,
) : ComposableView {

    @Composable
    override fun Draw(modifier: Modifier) {
        Column(modifier) {
            Spacer(Modifier.weight(1f))
            DrawTime()
            Spacer(Modifier.weight(3f))
            DrawTitle()
            Spacer(Modifier.weight(1f))
            DrawQuestion()
            Spacer(Modifier.weight(1f))
            DrawAnswer()
            Spacer(Modifier.weight(1f))
            DrawButtons()
            Spacer(Modifier.weight(5f))
        }
    }

    @Composable
    private fun ColumnScope.DrawTime() {
        val state = component.timeState.collectAsState().value
        Text(
            modifier = Modifier
                .align(Alignment.End)
                .padding(horizontal = 32.dp, vertical = 8.dp),
            text = stringResource(R.string.screen_game_time_left, state.value),
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
            text = stringResource(R.string.screen_game_current_question, state.current, state.total)
        )

        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 8.dp),
            text = stringResource(R.string.screen_game_correct_answers, state.correctCount)
        )
    }

    @Composable
    private fun ColumnScope.DrawQuestion() {
        val question = component.question.collectAsState().value
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 32.dp),
            text = "${question.title} = ?",
            style = MaterialTheme.typography.titleLarge,
        )
    }

    @Composable
    private fun ColumnScope.DrawAnswer() {
        val focusRequester = remember { FocusRequester() }
        val keyboard = LocalSoftwareKeyboardController.current

        val answer = component.answer.collectAsState()
        TextField(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 32.dp, vertical = 8.dp)
                .focusRequester(focusRequester),
            value = answer.value,
            onValueChange = component::onAnswerChanged,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { component.onDoneClicked() },
            ),
            label = { Text(stringResource(R.string.screen_game_field_answer_hint)) }
        )

        LaunchedEffect(focusRequester) {
            awaitFrame()
            focusRequester.requestFocus()
            keyboard?.show()
        }
    }

    @Composable
    private fun ColumnScope.DrawButtons() {
        val resultState = component.answerResult.collectAsState()
        AnimatedContent(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(horizontal = 32.dp, vertical = 8.dp),
            targetState = resultState.value,
        ) { result ->
            if (result == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Button(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = 28.dp),
                        onClick = component::onEnterClicked,
                        enabled = component.isEnterEnabled.collectAsState().value
                    ) {
                        Text(text = stringResource(R.string.screen_game_button_submit_answer))
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally),
                        text = stringResource(if (result) R.string.screen_game_result_correct else R.string.screen_game_result_wrong),
                        color = if (result) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleLarge,
                    )

                    Button(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(horizontal = 32.dp, vertical = 4.dp),
                        onClick = component::onNextClicked,
                    ) {
                        Text(text = stringResource(R.string.screen_game_next_question))
                    }
                }
            }
        }

    }
}

@Preview(showSystemUi = true, device = Devices.PIXEL_2)
@Preview(showSystemUi = true, device = Devices.PIXEL)
@Composable
private fun Preview() {
    val component = object : GameComponent {
        override val isEnterEnabled: StateFlow<Boolean> = MutableStateFlow(true)
        override val question: StateFlow<GameOptions.Question> = MutableStateFlow(GameOptions.Question.Multiplication(1, 3))
        override val questionsState: StateFlow<GameComponent.QuestionsState> =
            MutableStateFlow(GameComponent.QuestionsState(3, 1, 12))
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
            GameView(
                component = component,
            ).Draw(Modifier)
        }
    }
}
