package by.tigre.numbers.presentation.history

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import by.tigre.numbers.R
import by.tigre.numbers.presentation.challenge.result.ChallengeResultComponent
import by.tigre.numbers.presentation.challenge.result.ChallengeResultView
import by.tigre.numbers.presentation.game.result.ResultComponent
import by.tigre.numbers.presentation.utils.TIME_FORMAT
import by.tigre.numbers.presentation.utils.toLabel
import by.tigre.tools.tools.platform.compose.LocalGameColorsPalette
import by.tigre.tools.tools.platform.compose.ScreenComposableView
import by.tigre.tools.tools.platform.compose.view.ProgressIndicator
import by.tigre.tools.tools.platform.compose.view.ProgressIndicatorSize
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.launch

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
                    vector = ImageVector.vectorResource(
                        if (isVisible.value) {
                            by.tigre.numberscompose.R.drawable.outline_filter_alt_24
                        } else {
                            by.tigre.numberscompose.R.drawable.baseline_filter_list_alt_24
                        }
                    ),
                    action = { component.onFilterVisibleChanges(isVisible.value.not()) }
                )
            )
        }
    )
) {

    @Composable
    override fun Draw(modifier: Modifier) {
        val details by component.details.subscribeAsState()
        AnimatedContent(
            modifier = modifier,
            targetState = details.child?.instance,
            transitionSpec = { fadeIn().togetherWith(fadeOut()) }
        ) { item ->
            when (item) {
                is ResultComponent -> HistoryItemView(item, component::onCloseItemClicked).Draw(modifier)
                is ChallengeResultComponent -> ChallengeResultView(item).Draw(modifier)
                else -> super.Draw(modifier)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun DrawContent(innerPadding: PaddingValues) {
        val pagerState = component.pagerState
        val scope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            SecondaryTabRow(
                modifier = Modifier.fillMaxWidth(),
                selectedTabIndex = pagerState.currentPage,
            ) {
                Tab(
                    text = { Text(stringResource(R.string.screen_history_tab_tasks)) },
                    selected = pagerState.currentPage == 0,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(0) }
                    },
                )

                Tab(
                    text = { Text(stringResource(R.string.screen_history_tab_challenges)) },
                    selected = pagerState.currentPage == 1,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(1) }
                    },
                )
            }

            HorizontalPager(
                state = pagerState,
            ) { page ->
                if (page == 0) {
                    DrawTasksPage()
                } else {
                    DrawChallengesPage()
                }
            }
        }
    }

    @Composable
    private fun DrawTasksPage() {
        val results = component.resultsTasks.collectAsState()
        val isFilterVisible = component.filterVisibility.collectAsState()
        Column(
            Modifier
        ) {
            AnimatedVisibility(visible = isFilterVisible.value) {
                DrawFilterTasks()
            }

            AnimatedContent(
                targetState = results.value,
                transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                contentKey = { it::class.simpleName }
            ) { state ->
                when (state) {
                    is HistoryComponent.ScreenStateTasks.Loading -> DrawLoadingState()
                    is HistoryComponent.ScreenStateTasks.Empty -> DrawEmptyState(state.withFilter, isChallenge = false)
                    is HistoryComponent.ScreenStateTasks.History -> DrawHistoryItems(state, onItemClicked = component::onItemClicked)
                }
            }
        }
    }

    @Composable
    private fun DrawChallengesPage() {
        val results = component.resultsChallenges.collectAsState()
        val isFilterVisible = component.filterVisibility.collectAsState()
        Column(
            Modifier
        ) {
            AnimatedVisibility(visible = isFilterVisible.value) {
                DrawFilterChallenges()
            }

            AnimatedContent(
                targetState = results.value,
                transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                contentKey = { it::class.simpleName }
            ) { state ->
                when (state) {
                    is HistoryComponent.ScreenStateChallenges.Loading -> DrawLoadingState()
                    is HistoryComponent.ScreenStateChallenges.Empty -> DrawEmptyState(state.withFilter, isChallenge = true)
                    is HistoryComponent.ScreenStateChallenges.History -> DrawHistoryChallengeItems(
                        state,
                        onItemClicked = component::onItemClicked
                    )
                }
            }
        }
    }

    @Composable
    private fun DrawEmptyState(withFilter: Boolean, isChallenge: Boolean) {
        Box(
            Modifier
                .fillMaxSize()
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                text = stringResource(
                    when {
                        isChallenge && withFilter -> R.string.screen_history_empty_filter_challenges
                        isChallenge -> R.string.screen_history_empty_challenges
                        withFilter -> R.string.screen_history_empty_filter_tasks
                        else -> R.string.screen_history_empty_tasks
                    }
                ),
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

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun DrawHistoryItems(state: HistoryComponent.ScreenStateTasks.History, onItemClicked: (HistoryComponent.HistoryItem) -> Unit) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),

            ) {
            state.groups.forEach { group ->
                stickyHeader(key = group.date) {
                    DrawHistoryDayHeader(group = group)
                }

                if (group.isExpanded) {
                    group.items.forEachIndexed { index, item ->
                        item(key = item.id) {
                            DrawExpandedItem(item = item, isFirst = index == 0, onClick = { onItemClicked(item) })
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun DrawHistoryChallengeItems(
        state: HistoryComponent.ScreenStateChallenges.History,
        onItemClicked: (HistoryComponent.ChallengeItem) -> Unit
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),

            ) {
            state.challenges.forEachIndexed { index, challenge ->
                item {
                    Card(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, top = if (index == 0) 16.dp else 0.dp, bottom = 16.dp)
                            .fillMaxWidth()
                            .animateItem(),
                        border = if (challenge.isSuccess) {
                            BorderStroke(
                                width = 1.dp,
                                color = LocalGameColorsPalette.current.gameSuccess.color
                            )
                        } else {
                            null
                        },
                        onClick = { onItemClicked(challenge) }
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(
                                text = stringResource(R.string.screen_history_item_time, challenge.time)
                            )
                            Text(
                                text = stringResource(R.string.screen_history_item_duration, TIME_FORMAT.format(challenge.duration))
                            )
                            Text(
                                text = stringResource(R.string.screen_history_item_total_questions, challenge.totalCount)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun LazyItemScope.DrawHistoryDayHeader(
        group: HistoryComponent.HistoryGroup
    ) {
        Column(
            modifier = Modifier
                .animateItem(fadeInSpec = null, fadeOutSpec = null)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                .clickable { component.onGroupExpandChanges(group.isExpanded.not(), group) }
        ) {
            HorizontalDivider()
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
            HorizontalDivider()
        }
    }

    @Composable
    private fun LazyItemScope.DrawExpandedItem(item: HistoryComponent.HistoryItem, isFirst: Boolean, onClick: () -> Unit) {
        Card(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = if (isFirst) 16.dp else 0.dp, bottom = 16.dp)
                .fillMaxWidth()
                .animateItem(),
            border = if (item.totalCount == item.correctCount) {
                BorderStroke(
                    width = 1.dp,
                    color = LocalGameColorsPalette.current.gameSuccess.color
                )
            } else {
                null
            },
            onClick = onClick
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
    private fun DrawFilterTasks() {
        val filter = component.filter.collectAsState().value

        LazyVerticalGrid(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp))
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
                            selected = filter.onlySuccessTasks,
                            onClick = { component.onOnlySuccessTaskChanges(filter.onlySuccessTasks.not()) },
                            role = Role.Switch
                        )
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.screen_history_filter_game_only_success),
                    )

                    Checkbox(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        checked = filter.onlySuccessTasks,
                        onCheckedChange = null,
                    )
                }
            }
        }
    }

    @Composable
    private fun DrawFilterChallenges() {
        val onlySuccessChallenges = component.filter.collectAsState().value.onlySuccessChallenges

        LazyVerticalGrid(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp))
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item(span = { GridItemSpan(3) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = onlySuccessChallenges,
                            onClick = { component.onOnlySuccessChallengesChanges(onlySuccessChallenges.not()) },
                            role = Role.Switch
                        )
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.screen_history_filter_game_only_success),
                    )

                    Checkbox(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        checked = onlySuccessChallenges,
                        onCheckedChange = null,
                    )
                }
            }
        }
    }
}
