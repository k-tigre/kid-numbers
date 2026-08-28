package by.tigre.numbers.presentation.onboarding

import by.tigre.numbers.data.storage.Preferences
import by.tigre.numbers.domain.OnboardingRepository
import by.tigre.numbers.domain.OnboardingRepositoryImpl
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingComponentTest {

    @Test
    fun completingOnboardingHidesItOnNextLaunch() {
        val preferences: Preferences = InMemoryPreferences()
        val repository: OnboardingRepository = OnboardingRepositoryImpl(preferences)
        var navigatedToMenu: Boolean = false
        val component: OnboardingComponent = OnboardingComponentImpl(
            context = TestComponentContext(),
            onboardingRepository = repository,
            onComplete = { navigatedToMenu = true },
        )
        assertFalse(repository.isCompleted())
        component.onGetStartedClicked()
        assertTrue(navigatedToMenu)
        val relaunchRepository: OnboardingRepository = OnboardingRepositoryImpl(preferences)
        assertTrue(relaunchRepository.isCompleted())
    }

    private class InMemoryPreferences : Preferences {
        private val booleans: MutableMap<String, Boolean> = mutableMapOf()
        private val longs: MutableMap<String, Long> = mutableMapOf()
        private val strings: MutableMap<String, String> = mutableMapOf()

        override fun saveBoolean(key: String, value: Boolean?) {
            if (value == null) {
                booleans.remove(key)
            } else {
                booleans[key] = value
            }
        }

        override fun loadBoolean(key: String, default: Boolean): Boolean = booleans[key] ?: default

        override fun saveLong(key: String, value: Long?) {
            if (value == null) {
                longs.remove(key)
            } else {
                longs[key] = value
            }
        }

        override fun loadLong(key: String, default: Long): Long = longs[key] ?: default

        override fun saveString(key: String, value: String?) {
            if (value == null) {
                strings.remove(key)
            } else {
                strings[key] = value
            }
        }

        override fun loadString(key: String, default: String): String = strings[key] ?: default
    }

    private class TestComponentContext(
        componentContext: ComponentContext = DefaultComponentContext(lifecycle = LifecycleRegistry()),
        coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined),
    ) : by.tigre.tools.presentation.base.BaseComponentContext,
        ComponentContext by componentContext,
        CoroutineScope by coroutineScope
}
