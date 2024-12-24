package by.tigre.numbers.presentation.game.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.tigre.numbers.R
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameSettings.Equations
import by.tigre.numbers.presentation.game.settings.EquationsSettingsComponent.Settings
import by.tigre.numbers.presentation.utils.SelectableButton
import by.tigre.numbers.presentation.utils.toLabel
import by.tigre.tools.tools.platform.compose.AppTheme
import by.tigre.tools.tools.platform.compose.ScreenComposableView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EquationsSettingsView(
    private val component: EquationsSettingsComponent,
) : ScreenComposableView(
    ToolbarConfig.Default(
        title = { stringResource(R.string.screen_game_settings_title) },
        onBackClicked = component::onBackClicked
    )
) {

    @Composable
    override fun DrawContent(innerPadding: PaddingValues) {
        Column(Modifier.padding(innerPadding)) {
            val gridState: LazyGridState = rememberLazyGridState()
            val settings = component.settings.collectAsState().value

            LaunchedEffect("scroll") {
                component.onScrollPosition.collect { position ->
                    gridState.animateScrollToItem(position)
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

                item(key = "difficult_title", span = { GridItemSpan(6) }) {
                    Text(
                        modifier = Modifier,
                        text = stringResource(R.string.screen_game_settings_select_difficult)
                    )
                }

                settings.difficult.values.forEach { difficult ->
                    item(key = difficult, span = { GridItemSpan(2) }) {
                        Column(
                            Modifier
                                .heightIn(min = 56.dp)
                                .selectable(
                                    selected = settings.difficult.current == difficult,
                                    onClick = { component.onDifficultSelected(difficult) },
                                    role = Role.Switch
                                )
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            RadioButton(
                                selected = settings.difficult.current == difficult,
                                onClick = null
                            )

                            Text(
                                text = difficult.toLabel(),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }

                item(key = "range_title", span = { GridItemSpan(6) }) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    Text(
                        modifier = Modifier
                            .padding(top = 16.dp),
                        text = stringResource(R.string.screen_game_settings_select_numbers_for_equations)
                    )
                }

                settings.range.values.forEach { range ->
                    item(key = range, span = { GridItemSpan(3) }) {
                        SelectableButton(
                            isSelected = range == settings.range.current,
                            onClick = { component.onRangeSelected(range) }
                        ) {
                            Text(text = "${range.min} - ${range.max}")
                        }
                    }
                }

                item(key = "type", span = { GridItemSpan(6) }) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    Text(
                        modifier = Modifier
                            .padding(top = 16.dp),
                        text = stringResource(R.string.screen_game_settings_select_operation_type)
                    )
                }

                settings.type.values.forEach { type ->
                    item(key = type, span = { GridItemSpan(if (type == Equations.Type.Both) 6 else 3) }) {
                        SelectableButton(
                            isSelected = type == settings.type.current,
                            onClick = { component.onTypeSelected(type) }
                        ) {
                            Text(text = type.toLabel())
                        }
                    }
                }

                // TODO add dimension
//                item(key = "dimension", span = { GridItemSpan(6) }) {
//                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
//                    Text(
//                        modifier = Modifier
//                            .padding(top = 16.dp),
//                        text = "Количество неизвестных в уравнениях"
//                    )
//                }
//
//                settings.dimension.values.forEach { dimension ->
//                    item(key = dimension, span = { GridItemSpan(3) }) {
//                        SelectableButton(
//                            isSelected = dimension == settings.dimension.current,
//                            onClick = { component.onDimensionSelected(dimension) }
//                        ) {
//                            Text(text = "$dimension")
//                        }
//                    }
//                }
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

@Composable
@Preview
private fun Preview() {
    val component = object : EquationsSettingsComponent {
        override val onScrollPosition: Flow<Int>
            get() = TODO("Not yet implemented")
        override val settings: StateFlow<Settings> = MutableStateFlow(Settings.DEFAULTS)

        override fun onDifficultSelected(value: Difficult) {
            TODO("Not yet implemented")
        }

        override fun onTypeSelected(value: Equations.Type) {
            TODO("Not yet implemented")
        }

        override fun onRangeSelected(value: Equations.Range) {
            TODO("Not yet implemented")
        }

        override fun onDimensionSelected(value: Equations.Dimension) {
            TODO("Not yet implemented")
        }

        override fun onStartGameClicked() {
            TODO("Not yet implemented")
        }

        override fun onBackClicked() {
            TODO("Not yet implemented")
        }


    }
    AppTheme {
        Surface(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
        ) {
            EquationsSettingsView(
                component = component,
            ).Draw(Modifier)
        }
    }
}
