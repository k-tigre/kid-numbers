package by.tigre.numbers.presentation.game.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import by.tigre.numbers.R
import by.tigre.numbers.presentation.game.settings.SettingsUtils.drawDifficultSectionItems
import by.tigre.numbers.presentation.game.settings.SettingsUtils.drawRangeSectionItems
import by.tigre.tools.tools.platform.compose.ScreenComposableView
import kotlinx.coroutines.flow.debounce

class AdditionalSettingsView(
    private val component: AdditionalSettingsComponent,
) : ScreenComposableView(
    ToolbarConfig(
        title = { stringResource(R.string.screen_game_settings_title) },
        navigationIcon = ToolbarConfig.NavigationIconAction(action = component::onBackClicked)
    )
) {

    @Composable
    override fun DrawContent(innerPadding: PaddingValues) {
        Column(Modifier.padding(innerPadding)) {
            val gridState: LazyGridState = rememberLazyGridState()
            val settings = component.settings.collectAsState().value
            var selectedIndex by remember {
                mutableIntStateOf(-1)
            }

            LaunchedEffect("scroll") {
                component.onScrollPosition.collect { position ->
                    gridState.animateScrollToItem(position)
                    selectedIndex = position
                }
            }

            LaunchedEffect("unselect") {
                component.onScrollPosition
                    .debounce(1000)
                    .collect {
                        selectedIndex = -1
                    }
            }

            LazyVerticalGrid(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth()
                    .weight(1f)
                    .align(Alignment.CenterHorizontally),
                state = gridState,
                columns = GridCells.Fixed(6),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {


                drawDifficultSectionItems(
                    section = settings.difficult,
                    selectedIndex = selectedIndex,
                    onDifficultSelected = component::onDifficultSelected
                )

                drawRangeSectionItems(
                    section = settings.range,
                    selectedIndex = selectedIndex,
                    onRangeSelected = component::onRangeSelected
                )
            }

            Button(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp),
                onClick = component::onStartGameClicked,
                enabled = true
            ) {
                Text(text = stringResource(R.string.screen_game_settings_start))
            }
        }
    }
}
