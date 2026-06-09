package by.tigre.numbers.marketing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import android.content.ComponentName
import android.content.Context
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import by.tigre.numbers.R
import by.tigre.numbers.presentation.challenge.list.ListView
import by.tigre.numbers.presentation.game.GameView
import by.tigre.numbers.presentation.game.settings.MultiplicationSettingsView
import by.tigre.numbers.presentation.leaderboard.LeaderboardView
import by.tigre.numbers.presentation.menu.MenuView
import by.tigre.numbers.presentation.screenshot.ScreenshotGameComponent
import by.tigre.numbers.presentation.screenshot.ScreenshotGameMode
import by.tigre.numbers.presentation.statistic.StatisticView
import by.tigre.tools.tools.platform.compose.AppTheme
import com.github.takahirom.roborazzi.captureRoboImage
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

abstract class MarketingScreenshotTestBase(
    private val locale: MarketingScreenshotLocale,
) {
    @get:Rule(order = 0)
    val registerScreenshotTestActivityRule: TestWatcher = object : TestWatcher() {
        override fun starting(description: Description) {
            val appContext: Context = ApplicationProvider.getApplicationContext()
            Shadows.shadowOf(appContext.packageManager).addActivityIfNotPresent(
                ComponentName(
                    appContext.packageName,
                    ScreenshotTestActivity::class.java.name,
                ),
            )
        }
    }
    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<ScreenshotTestActivity>()
    @Test
    fun mainMenu() = captureScreen("01-main-menu") {
        MenuView(MarketingScreenshotFixtures.menuComponent()).Draw(Modifier.fillMaxSize())
    }
    @Test
    fun gameTimer() = captureScreen("02-game-timer") {
        GameView(ScreenshotGameComponent(ScreenshotGameMode.Timer)).Draw(Modifier.fillMaxSize())
    }
    @Test
    fun feedback() = captureScreen("03-feedback") {
        GameView(ScreenshotGameComponent(ScreenshotGameMode.Feedback)).Draw(Modifier.fillMaxSize())
    }
    @Test
    fun challenges() = captureScreen("04-challenges") {
        ListView(MarketingScreenshotFixtures.challengesComponent()).Draw(Modifier.fillMaxSize())
    }
    @Test
    fun leaderboard() = captureScreen("05-leaderboard") {
        LeaderboardView(MarketingScreenshotFixtures.leaderboardComponent(locale)).Draw(Modifier.fillMaxSize())
    }
    @Test
    fun statistics() = captureScreen("06-statistics") {
        StatisticView(MarketingScreenshotFixtures.statisticComponent()).Draw(Modifier.fillMaxSize())
    }
    @Test
    fun difficulty() = captureScreen("07-difficulty") {
        val confirmTitle: String = composeRule.activity.getString(R.string.screen_game_settings_start)
        MultiplicationSettingsView(
            component = MarketingScreenshotFixtures.multiplicationSettingsComponent(),
            confirmTitle = confirmTitle,
        ).Draw(Modifier.fillMaxSize())
    }
    private fun captureScreen(fileName: String, content: @androidx.compose.runtime.Composable () -> Unit) {
        composeRule.setContent {
            AppTheme {
                Surface(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .fillMaxSize()
                ) {
                    content()
                }
            }
        }
        composeRule.waitForIdle()
        val outputFile: File = screenshotsDir().resolve("$fileName.png")
        outputFile.parentFile?.mkdirs()
        composeRule.onRoot().captureRoboImage(outputFile.absolutePath)
    }
    private fun screenshotsDir(): File {
        return File("../docs/marketing/assets/screenshots/${locale.folderName}").absoluteFile
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "ru-rRU-w360dp-h640dp-xxhdpi")
class MarketingScreenshotRuTest : MarketingScreenshotTestBase(MarketingScreenshotLocale.Ru)

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "en-rUS-w360dp-h640dp-xxhdpi")
class MarketingScreenshotEnTest : MarketingScreenshotTestBase(MarketingScreenshotLocale.En)
