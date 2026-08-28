package by.tigre.numbers.presentation.onboarding

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import by.tigre.numbers.R
import by.tigre.tools.tools.platform.compose.ComposableView
import by.tigre.tools.tools.platform.compose.Dimens
import kotlinx.coroutines.launch

class OnboardingView(
    private val component: OnboardingComponent,
) : ComposableView {

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Draw(modifier: Modifier) {
        val pages: List<OnboardingPage> = listOf(
            OnboardingPage(
                icon = ImageVector.vectorResource(R.drawable.ic_menu_add),
                titleRes = R.string.onboarding_page1_title,
                bodyRes = R.string.onboarding_page1_body,
            ),
            OnboardingPage(
                icon = ImageVector.vectorResource(R.drawable.ic_menu_settings),
                titleRes = R.string.onboarding_page2_title,
                bodyRes = R.string.onboarding_page2_body,
            ),
            OnboardingPage(
                icon = ImageVector.vectorResource(R.drawable.ic_menu_leaderboard),
                titleRes = R.string.onboarding_page3_title,
                bodyRes = R.string.onboarding_page3_body,
            ),
        )
        val pagerState = rememberPagerState(pageCount = { pages.size })
        val scope = rememberCoroutineScope()
        val isLastPage: Boolean = pagerState.currentPage == pages.lastIndex
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(Dimens.lg),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = component::onSkipClicked) {
                    Text(text = stringResource(R.string.onboarding_skip))
                }
            }
            HorizontalPager(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = pagerState,
            ) { pageIndex ->
                DrawPage(page = pages[pageIndex])
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.md),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                pages.indices.forEach { index ->
                    val selected: Boolean = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = Dimens.xs)
                            .size(if (selected) 10.dp else 8.dp)
                            .background(
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                },
                                shape = CircleShape,
                            ),
                    )
                }
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (isLastPage) {
                        component.onGetStartedClicked()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
            ) {
                Text(
                    text = stringResource(
                        if (isLastPage) {
                            R.string.onboarding_get_started
                        } else {
                            R.string.onboarding_next
                        },
                    ),
                )
            }
        }
    }

    @Composable
    private fun DrawPage(page: OnboardingPage) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(page.titleRes),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Dimens.xl),
            )
            Text(
                text = stringResource(page.bodyRes),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Dimens.md),
            )
        }
    }

    private data class OnboardingPage(
        val icon: ImageVector,
        @StringRes val titleRes: Int,
        @StringRes val bodyRes: Int,
    )
}
