package by.tigre.numbers.marketing

import by.tigre.numbers.entity.Challenge
import by.tigre.numbers.entity.ChallengeWithCount
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameType
import by.tigre.numbers.entity.GameSettings
import by.tigre.numbers.entity.LeaderboardEntry
import by.tigre.numbers.entity.LeaderboardSpeedEntry
import by.tigre.numbers.entity.StatisticData
import by.tigre.numbers.presentation.challenge.list.ListComponent
import by.tigre.numbers.presentation.challenge.list.ListComponent.ChallengeItem
import by.tigre.numbers.presentation.game.settings.MultiplicationSettingsComponent
import by.tigre.numbers.presentation.leaderboard.LeaderboardComponent
import by.tigre.numbers.presentation.leaderboard.LeaderboardTab
import by.tigre.numbers.presentation.leaderboard.LeaderboardUiState
import by.tigre.numbers.presentation.menu.MenuComponent
import by.tigre.numbers.presentation.statistic.StatisticComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object MarketingScreenshotFixtures {
    fun menuComponent(): MenuComponent = object : MenuComponent {
        override val gameTypes: List<GameType> = GameType.entries
        override val hasActiveChallenge: StateFlow<Boolean> = MutableStateFlow(true)
        override val isLeaderboardEnabled: StateFlow<Boolean> = MutableStateFlow(false)
        override fun onGameClicked(type: GameType) = Unit
        override fun onHistoryClicked() = Unit
        override fun onChallengeClicked() = Unit
        override fun onStatisticClicked() = Unit
        override fun onLeaderboardClicked() = Unit
    }
    fun challengesComponent(): ListComponent = object : ListComponent {
        override val challenges: StateFlow<List<ChallengeItem>> = MutableStateFlow(challengeItems())
        override fun onCloseClicked() = Unit
        override fun onCreateClicked() = Unit
        override fun onViewClicked(challenge: ChallengeItem) = Unit
        override fun onStartClicked(challenge: ChallengeItem) = Unit
    }
    fun leaderboardComponent(locale: MarketingScreenshotLocale): LeaderboardComponent = object : LeaderboardComponent {
        private val now: Long = 1_700_000_000_000L
        private val speedSettings: GameSettings.Multiplication = GameSettings.Multiplication(
            selectedNumbers = listOf(2, 5, 10),
            difficult = Difficult.Easy,
            isPositive = true,
        )
        private val speedEntries: List<LeaderboardSpeedEntry> = when (locale) {
            MarketingScreenshotLocale.Ru -> listOf(
                LeaderboardSpeedEntry("Аня", 42, 0, now - 3_600_000),
                LeaderboardSpeedEntry("Максим", 48, 0, now - 7_200_000),
                LeaderboardSpeedEntry("София", 51, 0, now - 10_800_000),
                LeaderboardSpeedEntry("Илья", 55, 0, now - 14_400_000),
                LeaderboardSpeedEntry("Player", 58, 0, now - 18_000_000),
            )
            MarketingScreenshotLocale.En -> listOf(
                LeaderboardSpeedEntry("Anna", 42, 0, now - 3_600_000),
                LeaderboardSpeedEntry("Max", 48, 0, now - 7_200_000),
                LeaderboardSpeedEntry("Sophia", 51, 0, now - 10_800_000),
                LeaderboardSpeedEntry("Leo", 55, 0, now - 14_400_000),
                LeaderboardSpeedEntry("Player", 58, 0, now - 18_000_000),
            )
        }
        private val totalEntries: List<LeaderboardEntry> = when (locale) {
            MarketingScreenshotLocale.Ru -> listOf(
                LeaderboardEntry("Аня", 1240, 18, now - 3_600_000),
                LeaderboardEntry("Максим", 1180, 15, now - 7_200_000),
                LeaderboardEntry("София", 1050, 12, now - 10_800_000),
                LeaderboardEntry("Илья", 980, 11, now - 14_400_000),
                LeaderboardEntry("Player", 920, 9, now - 18_000_000),
            )
            MarketingScreenshotLocale.En -> listOf(
                LeaderboardEntry("Anna", 1240, 18, now - 3_600_000),
                LeaderboardEntry("Max", 1180, 15, now - 7_200_000),
                LeaderboardEntry("Sophia", 1050, 12, now - 10_800_000),
                LeaderboardEntry("Leo", 980, 11, now - 14_400_000),
                LeaderboardEntry("Player", 920, 9, now - 18_000_000),
            )
        }
        override val uiState: StateFlow<LeaderboardUiState> = MutableStateFlow(
            LeaderboardUiState(
                selectedTab = LeaderboardTab.Speed,
                speedBoardSettings = speedSettings,
                speedEntries = speedEntries,
                totalEntries = totalEntries,
                isLoading = false,
                errorMessage = null,
            )
        )
        override fun onBack() = Unit
        override fun onRefresh() = Unit
        override fun onTabSelected(tab: LeaderboardTab) = Unit
    }
    fun statisticComponent(): StatisticComponent = object : StatisticComponent {
        override val screenState: StateFlow<StatisticComponent.ScreenState> = MutableStateFlow(
            StatisticComponent.ScreenState.Data(
                statistic = StatisticData(
                    totalCorrect = 342,
                    totalAll = 410,
                    byType = mapOf(
                        GameType.Multiplication to StatisticData.TypeStatistic(correct = 156, total = 180),
                        GameType.Additional to StatisticData.TypeStatistic(correct = 98, total = 120),
                        GameType.Division to StatisticData.TypeStatistic(correct = 48, total = 60),
                        GameType.Subtraction to StatisticData.TypeStatistic(correct = 40, total = 50),
                    ),
                    avg7Days = StatisticData.PeriodAverage(correctPerDay = 12.4f, totalPerDay = 15.1f),
                    avg30Days = StatisticData.PeriodAverage(correctPerDay = 9.8f, totalPerDay = 11.6f),
                    avg7DaysByType = mapOf(
                        GameType.Multiplication to StatisticData.PeriodAverage(5.2f, 6.0f),
                        GameType.Additional to StatisticData.PeriodAverage(3.1f, 3.8f),
                    ),
                    avg30DaysByType = mapOf(
                        GameType.Multiplication to StatisticData.PeriodAverage(4.0f, 4.8f),
                        GameType.Additional to StatisticData.PeriodAverage(2.5f, 3.0f),
                    ),
                ),
                gameTypes = listOf(
                    GameType.Multiplication,
                    GameType.Additional,
                    GameType.Division,
                    GameType.Subtraction,
                ),
            )
        )
        override fun onCloseClicked() = Unit
    }
    fun multiplicationSettingsComponent(): MultiplicationSettingsComponent = object : MultiplicationSettingsComponent {
        override val isPositive: Boolean = true
        override val numbersForSelection: StateFlow<List<Pair<Int, Boolean>>> = MutableStateFlow(
            (1..9).map { number -> number to (number in listOf(2, 3, 5, 7, 8, 9)) }
        )
        override val difficultSelection: StateFlow<Difficult> = MutableStateFlow(Difficult.Medium)
        override val isStartEnabled: StateFlow<Boolean> = MutableStateFlow(true)
        override fun onNumberSelectionChanged(number: Int, isSelected: Boolean) = Unit
        override fun onDifficultChanged(difficult: Difficult) = Unit
        override fun onConfirmClicked() = Unit
        override fun onBackClicked() = Unit
    }
    private fun challengeItems(): List<ChallengeItem> = listOf(
        ChallengeItem(
            challenge = ChallengeWithCount(
                id = "1",
                taskCount = 3,
                startDate = 1_700_000_000_000L,
                status = Challenge.Status.Active,
                duration = Challenge.Duration.OneWeek,
            ),
            isDied = false,
        ),
        ChallengeItem(
            challenge = ChallengeWithCount(
                id = "2",
                taskCount = 2,
                startDate = -1,
                status = Challenge.Status.New,
                duration = Challenge.Duration.OneDay,
            ),
            isDied = false,
        ),
        ChallengeItem(
            challenge = ChallengeWithCount(
                id = "3",
                taskCount = 5,
                startDate = -1,
                status = Challenge.Status.New,
                duration = Challenge.Duration.TenMinutes,
            ),
            isDied = false,
        ),
    )
}
