package by.tigre.numbers.presentation.history

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.tigre.numbers.R
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameType
import by.tigre.numbers.presentation.utils.TIME_FORMAT
import by.tigre.numbers.presentation.utils.toLabel
import by.tigre.tools.tools.platform.compose.AppTheme
import by.tigre.tools.tools.platform.compose.LocalGameColorsPalette
import by.tigre.tools.tools.platform.compose.ScreenComposableView
import by.tigre.tools.tools.platform.compose.view.ProgressIndicator
import by.tigre.tools.tools.platform.compose.view.ProgressIndicatorSize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

class HistoryView(
    private val component: HistoryComponent,
) : ScreenComposableView(
    ToolbarConfig(
        title = { stringResource(R.string.screen_history_title) },
        navigationIcon = ToolbarConfig.NavigationIconAction(action = component::onCloseClicked),
        actions = {
            val isVisible = component.filterVisibility.collectAsState()

            listOf(
                ToolbarConfig.Action.Icon(
                    ImageVector.vectorResource(
                        if (isVisible.value) {
                            by.tigre.numberscompose.R.drawable.outline_filter_alt_24
                        } else {
                            by.tigre.numberscompose.R.drawable.baseline_filter_list_alt_24
                        }
                    ),
                    { component.onFilterVisibleChanges(isVisible.value.not()) }
                )
            )
        }
    )
) {

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun DrawHistoryItems(state: HistoryComponent.ScreenState.History) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            state.groups.forEach { group ->
                stickyHeader(key = group.date) {
                    DrawHistoryDayHeader(group = group)
                }

                if (group.isExpanded) {
                    group.items.forEach { item ->
                        item(key = item.id) {
                            DrawExpandedItem(item)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun DrawHistoryDayHeader(
        group: HistoryComponent.HistoryGroup
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .clickable { component.onGroupExpandChanges(group.isExpanded.not(), group) }
        ) {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = group.date,
                    style = MaterialTheme.typography.headlineSmall
                )

                AnimatedContent(group.isExpanded) { isExpanded ->
                    Icon(
                        modifier = Modifier.rotate(if (isExpanded) 180f else 0f),
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun LazyItemScope.DrawExpandedItem(item: HistoryComponent.HistoryItem) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .animateItemPlacement(),
            border = if (item.totalCount == item.correctCount) {
                BorderStroke(
                    width = 1.dp,
                    color = LocalGameColorsPalette.current.gameSuccess.color
                )
            } else {
                null
            }
        ) {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(
                    text = stringResource(R.string.screen_history_item_time, item.time)
                )
                Text(
                    text = stringResource(R.string.screen_history_item_duration, TIME_FORMAT.format(item.duration))
                )
                Text(
                    text = stringResource(R.string.screen_history_item_total_questions, item.totalCount)
                )
                Text(
                    text = stringResource(R.string.screen_history_item_correct_answers, item.correctCount)
                )
                Text(
                    text = stringResource(R.string.screen_history_item_difficulty, item.difficult.toLabel())
                )
                item.gameType?.let { gameType ->
                    Text(
                        text = stringResource(R.string.screen_history_item_game_type, gameType.toLabel())
                    )
                }
            }
        }
    }

    @Composable
    private fun DrawEmptyState() {
        Box(
            Modifier
                .fillMaxSize()
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                text = stringResource(R.string.screen_history_empty),
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    private fun DrawLoadingState() {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ProgressIndicator(size = ProgressIndicatorSize.LARGE)
        }
    }

    @Composable
    private fun DrawFilter() {
        val filter = component.filter.collectAsState().value

        LazyVerticalGrid(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(span = { GridItemSpan(3) }) {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    text = stringResource(R.string.screen_history_filter_difficult),
                )
            }

            filter.difficult.forEach { difficult ->
                item {
                    Column(
                        Modifier
                            .heightIn(min = 56.dp)
                            .selectable(
                                selected = difficult.value,
                                onClick = { component.onDifficultFilterChanges(difficult.key, difficult.value.not()) },
                                role = Role.Switch
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Checkbox(
                            checked = difficult.value,
                            onCheckedChange = null,
                        )

                        Text(
                            text = difficult.key.toLabel(),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }

            item(span = { GridItemSpan(3) }) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    text = stringResource(R.string.screen_history_filter_game_type),
                )
            }

            filter.gameType.forEach { type ->
                item {
                    Column(
                        Modifier
                            .heightIn(min = 56.dp)
                            .selectable(
                                selected = type.value,
                                onClick = { component.onGameTypeFilterChanges(type.key, type.value.not()) },
                                role = Role.Switch
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Checkbox(
                            checked = type.value,
                            onCheckedChange = null,
                        )

                        Text(
                            text = type.key.toLabel(),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }

            item(span = { GridItemSpan(3) }) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = filter.onlySuccess,
                            onClick = { component.onOnlySuccessChanges(filter.onlySuccess.not()) },
                            role = Role.Switch
                        )
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.screen_history_filter_game_only_success),
                    )

                    Checkbox(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        checked = filter.onlySuccess,
                        onCheckedChange = null,
                    )
                }
            }
        }
    }

    @Composable
    override fun DrawContent(innerPadding: PaddingValues) {
        val results = component.results.collectAsState()
        val isFilterVisible = component.filterVisibility.collectAsState()
        Column(
            Modifier
                .padding(innerPadding)
        ) {
            AnimatedVisibility(visible = isFilterVisible.value) {
                DrawFilter()
            }

            when (val state = results.value) {
                is HistoryComponent.ScreenState.Loading -> DrawLoadingState()
                is HistoryComponent.ScreenState.Empty -> DrawEmptyState()
                is HistoryComponent.ScreenState.History -> DrawHistoryItems(state)
            }
        }

    }
}

@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showSystemUi = true, device = Devices.NEXUS_10)
@Composable
private fun Preview() {
    val component = object : HistoryComponent {
        override val results: StateFlow<HistoryComponent.ScreenState> = MutableStateFlow(
            HistoryComponent.ScreenState.History(
                groups = listOf(HistoryComponent.HistoryGroup(
                    date = "2000-12-12",
                    isExpanded = true,
                    items = (1..20).map {
                        HistoryComponent.HistoryItem(
                            id = it.toLong(),
                            time = "12: 12",
                            duration = Random.nextLong(3_600_000),
                            difficult = Difficult.Hard,
                            correctCount = it * 10,
                            totalCount = 12,
                            gameType = GameType.Division
                        )
                    }
                )
                )
            )
        )

        override val filter: StateFlow<HistoryComponent.Filter> = MutableStateFlow(
            HistoryComponent.Filter(
                difficult = Difficult.entries.associateWith { true },
                gameType = GameType.entries.associateWith { true },
                onlySuccess = false
            )
        )
        override val filterVisibility = MutableStateFlow(true)

        override fun onFilterVisibleChanges(visible: Boolean) {
            filterVisibility.tryEmit(visible)
        }

        override fun onDifficultFilterChanges(difficult: Difficult, isEnabled: Boolean) = Unit
        override fun onGameTypeFilterChanges(type: GameType, isEnabled: Boolean) = Unit
        override fun onOnlySuccessChanges(isEnabled: Boolean) = Unit
        override fun onCloseClicked() = Unit
        override fun onGroupExpandChanges(isExpanded: Boolean, group: HistoryComponent.HistoryGroup) = Unit
    }

    AppTheme {
        Surface(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
        ) {
            HistoryView(
                component = component,
            ).Draw(Modifier)
        }
    }
}
