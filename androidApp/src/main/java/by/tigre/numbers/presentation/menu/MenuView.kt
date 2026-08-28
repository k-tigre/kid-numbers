package by.tigre.numbers.presentation.menu

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import by.tigre.numbers.R
import by.tigre.numbers.entity.GameType
import by.tigre.numbers.presentation.utils.toLabel
import by.tigre.tools.tools.platform.compose.ComposableView
import by.tigre.tools.tools.platform.compose.Dimens
import by.tigre.tools.tools.platform.compose.view.MenuCard
import by.tigre.tools.tools.platform.compose.view.SectionHeader

class MenuView(
    private val component: MenuComponent,
) : ComposableView {

    @Composable
    override fun Draw(modifier: Modifier) {
        val leaderboardEnabled = component.isLeaderboardEnabled.collectAsState().value
        BoxWithConstraints(modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .heightIn(max = maxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(start = Dimens.md, top = Dimens.lg, end = Dimens.md, bottom = Dimens.md),
                verticalArrangement = Arrangement.spacedBy(Dimens.sm),
            ) {
                Text(
                    text = stringResource(R.string.main_menu_app_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = stringResource(R.string.main_menu_tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SectionHeader(title = stringResource(R.string.main_menu_section_practice))
                component.gameTypes.forEach { type ->
                    MenuCard(
                        title = type.toLabel(),
                        icon = painterResource(gameTypeIcon(type)),
                        onClick = { component.onGameClicked(type) },
                    )
                }
                SectionHeader(title = stringResource(R.string.main_menu_section_compete))
                DrawChallengeItem()
                if (leaderboardEnabled) {
                    MenuCard(
                        title = stringResource(R.string.main_menu_leaderboard),
                        icon = painterResource(R.drawable.ic_menu_leaderboard),
                        onClick = component::onLeaderboardClicked,
                    )
                    MenuCard(
                        title = stringResource(R.string.main_menu_settings),
                        icon = painterResource(R.drawable.ic_menu_settings),
                        onClick = component::onSettingsClicked,
                    )
                }
                SectionHeader(title = stringResource(R.string.main_menu_section_progress))
                MenuCard(
                    title = stringResource(R.string.main_menu_statistic),
                    icon = painterResource(R.drawable.ic_menu_statistic),
                    onClick = component::onStatisticClicked,
                )
                MenuCard(
                    title = stringResource(R.string.main_menu_history),
                    icon = painterResource(R.drawable.ic_menu_history),
                    onClick = component::onHistoryClicked,
                )
            }
        }
    }

    @Composable
    private fun DrawChallengeItem() {
        val hasActiveChallenge = component.hasActiveChallenge.collectAsState().value
        MenuCard(
            title = stringResource(R.string.main_menu_challenges),
            subtitle = if (hasActiveChallenge) {
                stringResource(R.string.main_menu_challenges_active)
            } else {
                null
            },
            icon = painterResource(R.drawable.ic_menu_challenge),
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            onClick = component::onChallengeClicked,
        )
    }

    @DrawableRes
    private fun gameTypeIcon(type: GameType): Int = when (type) {
        GameType.Additional -> R.drawable.ic_menu_add
        GameType.Subtraction -> R.drawable.ic_menu_subtract
        GameType.Multiplication -> R.drawable.ic_menu_multiply
        GameType.Division -> R.drawable.ic_menu_divide
        GameType.Equations -> R.drawable.ic_menu_equation
    }
}
