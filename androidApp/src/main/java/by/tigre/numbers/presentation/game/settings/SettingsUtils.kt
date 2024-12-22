package by.tigre.numbers.presentation.game.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
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
import by.tigre.numbers.presentation.utils.toLabel
import kotlinx.coroutines.flow.StateFlow

object SettingsUtils {

    @Composable
    fun ColumnScope.DrawDifficult(
        difficult: StateFlow<Difficult>,
        onDifficultChanges: (Difficult) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        Text(
            modifier = modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 32.dp, vertical = 8.dp),
            text = stringResource(R.string.screen_game_settings_select_difficult)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            val current = difficult.collectAsState().value
            Difficult.entries.forEach { difficult ->
                Column(
                    Modifier
                        .heightIn(min = 56.dp)
                        .selectable(
                            selected = current == difficult,
                            onClick = { onDifficultChanges(difficult) },
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