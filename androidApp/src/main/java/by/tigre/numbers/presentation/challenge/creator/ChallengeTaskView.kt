package by.tigre.numbers.presentation.challenge.creator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import by.tigre.numbers.R
import by.tigre.numbers.entity.Challenge
import by.tigre.numbers.entity.GameSettings
import by.tigre.numbers.entity.GameType
import by.tigre.numbers.presentation.utils.TIME_FORMAT
import by.tigre.numbers.presentation.utils.toLabel
import by.tigre.tools.tools.platform.compose.ScreenComposableView
import com.arkivanov.decompose.extensions.compose.subscribeAsState

class ChallengeTaskView(private val component: DetailsComponent) : ScreenComposableView(
    ToolbarConfig(
        title = {
            when (component.mode.collectAsState().value) {
                DetailsComponent.Mode.View -> stringResource(R.string.screen_challenge_creator_menu_details)
                DetailsComponent.Mode.Edit -> stringResource(R.string.screen_challenge_creator_menu_edit)
                DetailsComponent.Mode.Creation -> stringResource(R.string.screen_challenge_creator_menu_create)
            }
        },
        navigationIcon = ToolbarConfig.NavigationIconAction(action = component::onCloseClicked),
        actions = {
            when (component.mode.collectAsState().value) {
                DetailsComponent.Mode.View -> listOf(
                    ToolbarConfig.Action.Icon(
                        vector = ImageVector.vectorResource(
                            R.drawable.baseline_edit_24
                        ),
                        action = component::onEditClicked
                    )
                )

                DetailsComponent.Mode.Edit,
                DetailsComponent.Mode.Creation -> listOf(
                    ToolbarConfig.Action.Icon(
                        vector = ImageVector.vectorResource(
                            R.drawable.baseline_done_24
                        ),
                        action = component::onSaveClicked
                    )
                )
            }
        }
    )
) {

    @Composable
    override fun Draw(modifier: Modifier) {
        super.Draw(modifier)

        val dialogSlot by component.dialogs.subscribeAsState()
        dialogSlot.child?.instance?.also {
            when (it) {
                DetailsComponent.DialogChild.ChallengeDuration -> DialogChallengeDuration()
                DetailsComponent.DialogChild.ConfirmRemove -> DialogConfirmRemove()
                DetailsComponent.DialogChild.TaskType -> DialogTaskType()
            }
        }
    }

    @Composable
    private fun DialogTaskType() {
        val radioOptions = GameType.entries
        val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }

        AlertDialog(
            onDismissRequest = component::onDismissDialog,
            title = { Text(text = stringResource(R.string.screen_challenge_creator_select_type_title)) },
            text = {
                Column(Modifier.selectableGroup()) {
                    radioOptions.forEach { option ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = (option == selectedOption),
                                    onClick = { onOptionSelected(option) },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (option == selectedOption),
                                onClick = null
                            )
                            Text(
                                text = option.toLabel(),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        component.onTaskTypeSelected(selectedOption)
                    }
                ) {
                    Text(stringResource(R.string.screen_challenge_creator_confirm))
                }
            },
            modifier = Modifier.width(300.dp),
        )
    }

    @Composable
    private fun DialogConfirmRemove() {
        AlertDialog(
            onDismissRequest = component::onDismissDialog,
            title = { Text(text = stringResource(R.string.screen_challenge_creator_remove_challenge_title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        component.onRemoveChallengeClicked()
                    }
                ) {
                    Text(stringResource(R.string.screen_challenge_creator_remove))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        component.onDismissDialog()
                    }
                ) {
                    Text(stringResource(R.string.screen_challenge_creator_cancel))
                }
            },
            modifier = Modifier.width(300.dp),
        )
    }

    @Composable
    private fun DialogChallengeDuration() {
        val radioOptions = Challenge.Duration.entries
        val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }

        AlertDialog(
            onDismissRequest = component::onDismissDialog,
            title = { Text(text = stringResource(R.string.screen_challenge_creator_select_duration_title)) },
            text = {
                Column(Modifier.selectableGroup()) {
                    radioOptions.forEach { option ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = (option == selectedOption),
                                    onClick = { onOptionSelected(option) },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (option == selectedOption),
                                onClick = null
                            )
                            Text(
                                text = option.toLabel(),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        component.onChallengeDurationSelected(selectedOption)
                    }
                ) {
                    Text(stringResource(R.string.screen_challenge_creator_confirm))
                }
            },
            modifier = Modifier.width(300.dp),
        )
    }

    @Composable
    override fun DrawContent(innerPadding: PaddingValues) {
        val tasks = component.tasks.collectAsState().value
        val isEditMode = when (component.mode.collectAsState().value) {
            DetailsComponent.Mode.View -> false
            DetailsComponent.Mode.Edit,
            DetailsComponent.Mode.Creation -> true
        }

        if (tasks.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                DrawDurations()

                Button(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.Center),
                    onClick = component::onAddClicked
                ) {
                    Text(stringResource(R.string.screen_challenge_creator_add_task_button))
                }
            }
        } else {
            Column(
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {

                DrawDurations()

                LazyColumn(Modifier) {
                    tasks.forEachIndexed { index, task ->
                        item { DrawTaskItem(task, isFirst = index == 0, isEditMode = isEditMode) }
                    }

                    if (isEditMode) {
                        item {
                            Box(Modifier.fillMaxWidth()) {
                                Button(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .align(Alignment.Center),
                                    onClick = component::onAddClicked
                                ) {
                                    Text(stringResource(R.string.screen_challenge_creator_add_more_button))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun DrawDurations() {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            val durations = component.durations.collectAsState().value

            if (durations.selected != null) {
                Text(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    text = stringResource(R.string.screen_challenge_creator_selected_duration, durations.selected.toLabel())
                )
                Text(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    text = stringResource(R.string.screen_challenge_creator_tasks_duration, TIME_FORMAT.format(durations.total))
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
        }
    }

    @Composable
    private fun LazyItemScope.DrawTaskItem(item: Challenge.Task, isFirst: Boolean, isEditMode: Boolean) {
        Card(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = if (isFirst) 16.dp else 0.dp, bottom = 16.dp)
                .fillMaxWidth()
                .animateItem(),
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .weight(1f)
                ) {
                    val gameType = when (item.gameSettings) {
                        is GameSettings.Additional -> if (item.gameSettings.isPositive) GameType.Additional else GameType.Subtraction
                        is GameSettings.Equations -> GameType.Equations
                        is GameSettings.Multiplication -> if (item.gameSettings.isPositive) GameType.Multiplication else GameType.Division
                    }

                    Text(
                        text = stringResource(R.string.screen_history_item_game_type, gameType.toLabel())
                    )

                    Text(
                        text = stringResource(
                            R.string.screen_history_item_difficulty,
                            item.gameSettings.difficult.toLabel()
                        )
                    )

                    when (item.gameSettings) {
                        is GameSettings.Additional -> DrawItemRange(item.gameSettings.range)
                        is GameSettings.Equations -> DrawItemRange(item.gameSettings.range)
                        is GameSettings.Multiplication -> DrawItemNumberCounts(item.gameSettings.selectedNumbers.size)
                    }
                }

                if (isEditMode) {
                    IconButton(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterVertically),
                        onClick = { component.onRemoveClicked(item) }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_delete_outline_24),
                            contentDescription = "remove"
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun DrawItemRange(range: GameSettings.Range) {
        Text(
            text = stringResource(R.string.screen_challenge_settings_number_range, range.min, range.max)
        )
    }

    @Composable
    private fun DrawItemNumberCounts(count: Int) {
        Text(
            text = stringResource(R.string.screen_challenge_settings_number_counts, count)
        )
    }
}