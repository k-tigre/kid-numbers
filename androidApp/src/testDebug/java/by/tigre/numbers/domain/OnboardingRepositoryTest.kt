package by.tigre.numbers.domain

import by.tigre.numbers.data.storage.Preferences
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingRepositoryTest {

    private val preferences: Preferences = InMemoryPreferences()
    private val repository: OnboardingRepository = OnboardingRepositoryImpl(preferences)

    @Test
    fun isCompletedDefaultsToFalse() {
        assertFalse(repository.isCompleted())
    }

    @Test
    fun markCompletedPersistsTrue() {
        repository.markCompleted()
        assertTrue(repository.isCompleted())
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
}
