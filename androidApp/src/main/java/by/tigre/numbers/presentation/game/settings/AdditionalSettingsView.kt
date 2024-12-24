package by.tigre.numbers.presentation.game.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import by.tigre.numbers.R
import by.tigre.numbers.presentation.game.settings.SettingsUtils.DrawDifficult
import by.tigre.numbers.presentation.utils.SelectableButton
import by.tigre.tools.tools.platform.compose.ScreenComposableView

class AdditionalSettingsView(
    private val component: AdditionalSettingsComponent,
) : ScreenComposableView(
    ToolbarConfig.Default(
        title = { stringResource(R.string.screen_game_settings_title) },
        onBackClicked = component::onBackClicked
    )
) {

    @Composable
    override fun DrawContent(innerPadding: PaddingValues) {
        Column(Modifier.padding(innerPadding)) {
            DrawDifficult(
                onDifficultChanges = component::onDifficultChanged,
                difficult = component.difficultSelection
            )
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 32.dp),
                text = stringResource(
                    if (component.isPositive) {
                        R.string.screen_game_settings_select_numbers_for_addition
                    } else {
                        R.string.screen_game_settings_select_numbers_for_subtraction
                    }
                )
            )

            val numbers = component.numbersForSelection.collectAsState()
            LazyVerticalGrid(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(320.dp)
                    .align(Alignment.CenterHorizontally),
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                numbers.value.forEach { (number, isSelected) ->
                    item(key = number) {
                        SelectableButton(
                            isSelected = isSelected,
                            onClick = {
                                component.onNumberTypeSelectionChanged(range = number, isSelected = isSelected.not())
                            }
                        ) {
                            Text(text = "${number.min} - ${number.max}")
                        }
                    }
                }
            }

            Button(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp),
                onClick = component::onStartGameClicked,
                enabled = component.isStartEnabled.collectAsState().value
            ) {
                Text(text = stringResource(R.string.screen_game_settings_start))
            }
        }
    }
}
