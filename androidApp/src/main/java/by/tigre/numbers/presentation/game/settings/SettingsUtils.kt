package by.tigre.numbers.presentation.game.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import by.tigre.numbers.R
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameSettings
import by.tigre.numbers.entity.GameSettings.Equations
import by.tigre.numbers.presentation.utils.SelectableButton
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

    fun LazyGridScope.drawDifficultSectionItems(section: DifficultSection, selectedIndex: Int, onDifficultSelected: (Difficult) -> Unit) {
        item(key = "difficult_title", span = { GridItemSpan(6) }) {
            Text(
                modifier = Modifier,
                text = stringResource(R.string.screen_game_settings_select_difficult),
                color = getTitleColor(selectedIndex, section.index)
            )
        }

        section.values.forEach { difficult ->
            item(key = difficult, span = { GridItemSpan(2) }) {
                Column(
                    Modifier
                        .heightIn(min = 56.dp)
                        .selectable(
                            selected = section.current == difficult,
                            onClick = { onDifficultSelected(difficult) },
                            role = Role.Switch
                        )
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RadioButton(
                        selected = section.current == difficult,
                        onClick = null
                    )

                    Text(
                        text = difficult.toLabel(),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }

    fun LazyGridScope.drawRangeSectionItems(section: RangeSection, selectedIndex: Int, onRangeSelected: (GameSettings.Range) -> Unit) {
        item(key = "range_title", span = { GridItemSpan(6) }) {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
            val scale = getTitleScale(selectedIndex, section.index)
            Text(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin.Center
                    }
                    .padding(top = 16.dp),
                text = stringResource(R.string.screen_game_settings_select_numbers_for_equations),
                color = getTitleColor(selectedIndex, section.index)
            )
        }

        section.values.forEach { range ->
            item(key = range, span = { GridItemSpan(3) }) {
                SelectableButton(
                    isSelected = range == section.current,
                    onClick = { onRangeSelected(range) }
                ) {
                    Text(text = "${range.min} - ${range.max}")
                }
            }
        }
    }

    fun LazyGridScope.drawTypeSectionItems(section: TypeSection, selectedIndex: Int, onTypeSelected: (Equations.Type) -> Unit) {
        item(key = "type", span = { GridItemSpan(6) }) {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
            val scale = getTitleScale(selectedIndex, section.index)
            Text(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin.Center
                    }
                    .padding(top = 16.dp),
                text = stringResource(R.string.screen_game_settings_select_operation_type),
                color = getTitleColor(selectedIndex, section.index)
            )
        }

        section.values.forEach { type ->
            item(key = type, span = { GridItemSpan(if (type == Equations.Type.Both) 6 else 3) }) {
                SelectableButton(
                    isSelected = type == section.current,
                    onClick = { onTypeSelected(type) }
                ) {
                    Text(text = type.toLabel())
                }
            }
        }
    }

    @Composable
    private fun getTitleColor(selectedIndex: Int, target: Int): Color {
        val color by animateColorAsState(
            if (selectedIndex == target) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            label = "title_color"
        )
        return color
    }

    @Composable
    private fun getTitleScale(selectedIndex: Int, target: Int): Float {
        val scale by animateFloatAsState(
            if (selectedIndex == target) 1.05f else 1f,
            label = "title_scale"
        )
        return scale
    }

    data class DifficultSection(
        val current: Difficult?,
        val values: List<Difficult>,
        val index: Int,
    )

    data class RangeSection(
        val current: GameSettings.Range?,
        val values: List<GameSettings.Range>,
        val index: Int,
    )

    data class TypeSection(
        val current: Equations.Type?,
        val values: List<Equations.Type>,
        val index: Int,
    )

    data class DimensionSection(
        val current: Equations.Dimension?,
        val values: List<Equations.Dimension>,
        val index: Int,
    )
}