package by.tigre.numbers.presentation.challenge.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.tigre.numbers.R
import by.tigre.numbers.entity.Challenge
import by.tigre.numbers.entity.ChallengeWithCount
import by.tigre.numbers.presentation.challenge.list.ListComponent.ChallengeItem
import by.tigre.numbers.presentation.utils.toLabel
import by.tigre.tools.tools.platform.compose.AppTheme
import by.tigre.tools.tools.platform.compose.ScreenComposableView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ListView(
    private val component: ListComponent,
) : ScreenComposableView(
    ToolbarConfig(
        title = { stringResource(R.string.screen_challenge_list_title) },
        navigationIcon = ToolbarConfig.NavigationIconAction(action = component::onCloseClicked),
    )
) {

    @Composable
    override fun DrawContent(innerPadding: PaddingValues) {
        val challenges = component.challenges.collectAsState().value
        if (challenges.isNotEmpty()) {
            LazyColumn(
                Modifier
                    .padding(innerPadding)
                    .fillMaxWidth()
            ) {
                items(items = challenges) {
                    DrawChallenge(it)
                }

                item {
                    Box(Modifier.fillMaxWidth()) {
                        Button(
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.Center),
                            onClick = component::onCreateClicked
                        ) {
                            Text(stringResource(R.string.screen_challenge_list_add_more))
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .padding(8.dp),
                    text = stringResource(R.string.screen_challenge_list_empty_title)
                )

                Button(
                    modifier = Modifier
                        .padding(8.dp),
                    onClick = component::onCreateClicked
                ) {
                    Text(stringResource(R.string.screen_challenge_list_empty_add_button))
                }
            }
        }
    }

    @Composable
    private fun LazyItemScope.DrawChallenge(challenge: ChallengeItem) {
        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .animateItem(),
            onClick = { component.onViewClicked(challenge) }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.screen_challenge_list_item_title)
                )
            }
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {


                when {
                    challenge.isDied -> {
                        Text(
                            text = stringResource(R.string.screen_challenge_list_item_timeout),
                            color = MaterialTheme.colorScheme.error
                        )

                        Text(
                            text = stringResource(R.string.screen_challenge_list_item_not_finished_tasks, challenge.challenge.taskCount)
                        )

                        Button(
                            modifier = Modifier
                                .padding(8.dp),
                            onClick = { component.onStartClicked(challenge) }
                        ) {
                            Text(stringResource(R.string.button_close))
                        }
                    }

                    challenge.challenge.status == Challenge.Status.Active -> {
                        Text(
                            text = stringResource(
                                R.string.screen_challenge_list_item_active_duration,
                                challenge.challenge.duration.toLabel()
                            )
                        )
                        Text(
                            text = stringResource(R.string.screen_challenge_list_item_active_left_task, challenge.challenge.taskCount)
                        )

                        Button(
                            modifier = Modifier
                                .padding(8.dp),
                            onClick = { component.onStartClicked(challenge) }
                        ) {
                            Text(stringResource(R.string.button_continue))
                        }
                    }

                    else -> {
                        Text(
                            text = stringResource(R.string.screen_challenge_list_item_new_duration, challenge.challenge.duration.toLabel())
                        )
                        Text(
                            text = stringResource(R.string.screen_challenge_list_item_new_tasks_count, challenge.challenge.taskCount)
                        )

                        Button(
                            modifier = Modifier
                                .padding(8.dp),
                            onClick = { component.onStartClicked(challenge) }
                        ) {
                            Text(stringResource(R.string.button_start))
                        }
                    }
                }
            }
        }
    }
}

private fun mockComponent(challenges: List<ChallengeItem>) = object : ListComponent {
    override val challenges: StateFlow<List<ChallengeItem>> = MutableStateFlow(challenges)

    override fun onCloseClicked() = Unit
    override fun onCreateClicked() = Unit
    override fun onViewClicked(challenge: ChallengeItem) = Unit
    override fun onStartClicked(challenge: ChallengeItem) = Unit
}

@Preview(showSystemUi = true, device = Devices.PIXEL)
@Composable
private fun PreviewEmpty() {
    AppTheme {
        Surface(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
        ) {
            ListView(
                component = mockComponent(emptyList()),
            ).Draw(Modifier)
        }
    }
}

@Preview(showSystemUi = true, device = Devices.PIXEL)
@Composable
private fun Preview() {
    AppTheme {
        Surface(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
        ) {
            ListView(
                component = mockComponent(
                    listOf(
                        ChallengeItem(
                            ChallengeWithCount("1", 1, System.currentTimeMillis(), Challenge.Status.Active, Challenge.Duration.TenMinutes),
                            isDied = true
                        ),
                        ChallengeItem(
                            ChallengeWithCount("2", 3, -1, Challenge.Status.New, Challenge.Duration.OneDay),
                            isDied = false
                        ),
                        ChallengeItem(
                            ChallengeWithCount("2", 5, -1, Challenge.Status.New, Challenge.Duration.OneWeek),
                            isDied = false
                        )
                    )
                ),
            ).Draw(Modifier)
        }
    }
}
