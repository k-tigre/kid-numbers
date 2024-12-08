package by.tigre.numbers.presentation.game.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import by.tigre.tools.tools.platform.compose.ScreenComposableView

class MultiplicationSettingsView(
    private val component: MultiplicationSettingsComponent,
) : ScreenComposableView(
    ToolbarConfig.Default(
        title = { "Настройки сложности" },
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
                text = if (component.isPositive) {
                    "Выбери цифры, с которыми хочешь проверить умножение"
                } else {
                    "Выбери цифры, с которыми хочешь проверить деление"
                }
            )

            val numbers = component.numbersForSelection.collectAsState()
            LazyVerticalGrid(
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.CenterHorizontally),
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                numbers.value.forEach { (number, isSelected) ->
                    item(key = number) {

                        if (isSelected) {
                            Button(onClick = { component.onNumberSelectionChanged(number, isSelected.not()) }) {
                                Text(text = "$number")
                            }
                        } else {
                            ElevatedButton(onClick = { component.onNumberSelectionChanged(number, isSelected.not()) }) {
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
                Text(text = "Начать")
            }
        }
    }

    @Composable
    private fun ColumnScope.DrawDifficult() {
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(32.dp),
            text = "Выбери сложность"
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
