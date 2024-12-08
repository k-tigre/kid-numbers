package by.tigre.numbers.presentation.game.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import by.tigre.numbers.R
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameSettings
import by.tigre.numbers.presentation.utils.toLabel
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
            DrawDifficult()

            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(32.dp),
                text = stringResource(
                    if (component.isPositive) {
                        R.string.screen_game_settings_select_numbers_for_addition
                    } else {
                        R.string.screen_game_settings_select_numbers_for_subtraction
                    }
                )
            )

            val numbers = component.numbersForSelection.collectAsState()
            LazyColumn(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),

                ) {
                numbers.value.forEach { (number, isSelected) ->
                    item(key = number) {
                        val range = when (number) {
                            GameSettings.NumberType.Single -> "0-10"
                            GameSettings.NumberType.Double -> "10-100"
                            GameSettings.NumberType.Triples -> "100-1000"
                            GameSettings.NumberType.SingleDoubleTriples -> "0-1000"
                            GameSettings.NumberType.SingleDouble -> "0-100"
                        }
                        if (isSelected) {
                            Button(onClick = { component.onNumberTypeSelectionChanged(type = number, isSelected = false) }) {
                                Text(text = stringResource(R.string.screen_game_settings_number_range, range))
                            }
                        } else {
                            ElevatedButton(onClick = { component.onNumberTypeSelectionChanged(type = number, isSelected = true) }) {
                                Text(text = stringResource(R.string.screen_game_settings_number_range, range))
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


    @Composable
    private fun ColumnScope.DrawDifficult() {
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(32.dp),
            text = stringResource(R.string.screen_game_settings_select_difficult)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            val current = component.difficultSelection.collectAsState().value
            Difficult.entries.forEach { difficult ->
                Column(
                    Modifier
                        .heightIn(min = 56.dp)
                        .selectable(
                            selected = current == difficult,
                            onClick = { component.onDifficultChanged(difficult) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RadioButton(
                        selected = current == difficult,
                        onClick = null // null recommended for accessibility with screenreaders
                    )

                    Text(
                        text = difficult.toLabel(),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding()
                    )
                }
            }
        }
    }
}
