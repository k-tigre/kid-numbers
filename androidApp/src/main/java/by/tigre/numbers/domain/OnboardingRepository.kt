package by.tigre.numbers.domain

import by.tigre.numbers.data.storage.Preferences

interface OnboardingRepository {
    fun isCompleted(): Boolean
    fun markCompleted()
}

class OnboardingRepositoryImpl(
    private val preferences: Preferences,
) : OnboardingRepository {

    override fun isCompleted(): Boolean {
        return preferences.loadBoolean(KEY_ONBOARDING_COMPLETED, default = false)
    }

    override fun markCompleted() {
        preferences.saveBoolean(KEY_ONBOARDING_COMPLETED, value = true)
    }

    private companion object {
        const val KEY_ONBOARDING_COMPLETED: String = "onboarding_completed"
    }
}
