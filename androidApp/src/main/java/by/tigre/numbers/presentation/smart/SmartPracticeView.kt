package by.tigre.numbers.presentation.smart

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import by.tigre.numbers.R
import by.tigre.numbers.entity.FactKey
import by.tigre.numbers.presentation.game.settings.gameSettingsConfirmButtonColors
import by.tigre.tools.tools.platform.compose.Dimens
import by.tigre.tools.tools.platform.compose.ScreenComposableView
import by.tigre.tools.tools.platform.compose.view.SectionHeader

class SmartPracticeView(
    private val component: SmartPracticeComponent,
) : ScreenComposableView(
    ToolbarConfig(
        title = { stringResource(R.string.smart_practice_title) },
        navigationIcon = ToolbarConfig.NavigationIconAction(action = component::onBack),
    )
) {

    @Composable
    override fun DrawContent(innerPadding: PaddingValues) {
        val selectedOps = component.selectedOps.collectAsState().value
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = Dimens.md),
        ) {
            SectionHeader(title = stringResource(R.string.smart_practice_section_ops))
            component.visibleOps.forEach { op ->
                val isSelected: Boolean = op in selectedOps
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = isSelected,
                            onClick = { component.onOpToggled(op) },
                            role = Role.Checkbox,
                        )
                        .padding(vertical = Dimens.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = null,
                    )
                    Text(
                        text = stringResource(opLabelRes(op)),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = Dimens.sm),
                    )
                }
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.lg, bottom = Dimens.md),
                onClick = component::onStart,
                enabled = component.isStartEnabled.collectAsState().value,
                colors = gameSettingsConfirmButtonColors(),
            ) {
                Text(text = stringResource(R.string.smart_practice_start))
            }
        }
    }

    @StringRes
    private fun opLabelRes(op: FactKey.Op): Int = when (op) {
        FactKey.Op.ADD -> R.string.smart_practice_op_add
        FactKey.Op.SUB -> R.string.smart_practice_op_sub
        FactKey.Op.MUL -> R.string.smart_practice_op_mul
        FactKey.Op.DIV -> R.string.smart_practice_op_div
    }
}
