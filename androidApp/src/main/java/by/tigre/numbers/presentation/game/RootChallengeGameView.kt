package by.tigre.numbers.presentation.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import by.tigre.numbers.R
import by.tigre.numbers.presentation.challenge.result.ChallengeResultView
import by.tigre.numbers.presentation.game.result.ResultView
import by.tigre.tools.tools.platform.compose.ComposableView
import com.arkivanov.decompose.extensions.compose.jetpack.stack.Children
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.stackAnimation

class RootChallengeGameView(
    private val component: RootChallengeGameComponent,
) : ComposableView {

    @Composable
    override fun Draw(modifier: Modifier) {
        Column(modifier = modifier) {
            DrawState()

            Children(
                modifier = Modifier,
                stack = component.pages,
                animation = stackAnimation(animator = fade())
            ) {
                when (val child = it.instance) {
                    is RootChallengeGameComponent.PageChild.Game -> GameView(child.component)
                    is RootChallengeGameComponent.PageChild.GameResult -> ResultView(child.component)
                    is RootChallengeGameComponent.PageChild.ChallengeResult -> ChallengeResultView(child.component)
                }.Draw(modifier = Modifier.fillMaxSize())
            }
        }
    }

    @Composable
    private fun DrawState() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp))
        ) {
            val state = component.state.collectAsState().value

            if (state.isCompleted.not()) {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp),
                    text = stringResource(R.string.screen_game_challenge_task_count, state.completedTaskCount, state.taskCount)
                )

                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 8.dp),
                    text = stringResource(R.string.screen_game_challenge_duration, state.time)
                )
            }
        }
    }
}
