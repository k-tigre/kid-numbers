package by.tigre.numbers.presentation.game.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameSettings
import by.tigre.tools.tools.platform.compose.ComposableView

class AdditionalSettingsView(
    private val component: AdditionalSettingsComponent,
) : ComposableView {

    @Composable
    override fun Draw(modifier: Modifier) {
        Column(modifier) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(32.dp),
                text = "Выбери сложность"
            )

            DrawDifficult()

            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(32.dp),
                text = "Выбери порядок чисел, с которыми хочешь проверить сложение"
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
                        val title = when (number) {
                            GameSettings.Additional.NumberType.Single -> "0-10"
                            GameSettings.Additional.NumberType.Double -> "10-100"
                            GameSettings.Additional.NumberType.Triples -> "100-1000"
                            GameSettings.Additional.NumberType.SingleDoubleTriples -> "0-1000"
                            GameSettings.Additional.NumberType.SingleDouble -> "0-100"
                        }
                        if (isSelected) {
                            Button(onClick = { component.onNumberTypeSelectionChanged(number, isSelected.not()) }) {
                                Text(text = "Числа в диапазоне $title")
                            }
                        } else {
                            ElevatedButton(onClick = { component.onNumberTypeSelectionChanged(number, isSelected.not()) }) {
                                Text(text = "Числа в диапазоне $title")
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
                Text(text = "Начать")
            }
        }
    }


    @Composable
    private fun DrawDifficult() {
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
                        text = when (difficult) {
                            Difficult.Easy -> "Просто"
                            Difficult.Medium -> "Средне"
                            Difficult.Hard -> "Сложно"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding()
                    )
                }
            }
        }
    }
}
