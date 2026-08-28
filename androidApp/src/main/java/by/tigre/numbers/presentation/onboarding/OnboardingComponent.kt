package by.tigre.numbers.presentation.onboarding

import by.tigre.numbers.domain.OnboardingRepository
import by.tigre.tools.presentation.base.BaseComponentContext

interface OnboardingComponent {
    fun onSkipClicked()
    fun onGetStartedClicked()
}

class OnboardingComponentImpl(
    context: BaseComponentContext,
    private val onboardingRepository: OnboardingRepository,
    private val onComplete: () -> Unit,
) : OnboardingComponent, BaseComponentContext by context {

    override fun onSkipClicked() = complete()

    override fun onGetStartedClicked() = complete()

    private fun complete() {
        onboardingRepository.markCompleted()
        onComplete()
    }
}
