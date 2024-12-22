package by.tigre.numbers.presentation.game.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import by.tigre.numbers.R
import by.tigre.numbers.presentation.game.settings.SettingsUtils.DrawDifficult
import by.tigre.tools.tools.platform.compose.ScreenComposableView

class MultiplicationSettingsView(
    private val component: MultiplicationSettingsComponent,
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
                    .padding(start = 32.dp, end = 32.dp, top = 16.dp),
                text = stringResource(
                    if (component.isPositive) {
                        R.string.screen_game_settings_select_numbers_for_multiplication
                    } else {
                        R.string.screen_game_settings_select_numbers_for_division
                    }
                )
            )

            val numbers = component.numbersForSelection.collectAsState()
            LazyVerticalGrid(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(320.dp)
                    .align(Alignment.CenterHorizontally),
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                numbers.value.forEach { (number, isSelected) ->
                    item(key = number) {

                        if (isSelected) {
                            Button(onClick = { component.onNumberSelectionChanged(number = number, isSelected = false) }) {
                                Text(text = "$number")
                            }
                        } else {
                            ElevatedButton(onClick = { component.onNumberSelectionChanged(number = number, isSelected = true) }) {
                                Text(text = "$number")
                            }
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
