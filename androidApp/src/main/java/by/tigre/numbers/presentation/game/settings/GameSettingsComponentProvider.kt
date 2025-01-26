package by.tigre.numbers.presentation.game.settings

import by.tigre.numbers.analytics.EventAnalytics
import by.tigre.numbers.entity.GameSettings
import by.tigre.tools.presentation.base.BaseComponentContext
import by.tigre.tools.tools.coroutines.CoreDispatchers

interface GameSettingsComponentProvider {
    fun createMultiplicationSettingsComponent(isPositive: Boolean, context: BaseComponentContext): MultiplicationSettingsComponent
    fun createAdditionalSettingsComponent(isPositive: Boolean, context: BaseComponentContext): AdditionalSettingsComponent
    fun createEquationsSettingsComponent(context: BaseComponentContext): EquationsSettingsComponent

    class Impl(
        private val dispatchers: CoreDispatchers,
        private val analytics: EventAnalytics,
        private val onClose: () -> Unit,
        private val onConfirmSettings: (GameSettings) -> Unit
    ) : GameSettingsComponentProvider {
        override fun createMultiplicationSettingsComponent(
            isPositive: Boolean,
            context: BaseComponentContext
        ): MultiplicationSettingsComponent = MultiplicationSettingsComponent.Impl(
            context = context,
            isPositive = isPositive,
            onStartGame = onConfirmSettings,
            onClose = onClose
        )

        override fun createAdditionalSettingsComponent(isPositive: Boolean, context: BaseComponentContext): AdditionalSettingsComponent =
            AdditionalSettingsComponent.Impl(
                context = context,
                isPositive = isPositive,
                onStartGame = onConfirmSettings,
                onClose = onClose,
                analytics = analytics
            )

        override fun createEquationsSettingsComponent(context: BaseComponentContext): EquationsSettingsComponent =
            EquationsSettingsComponent.Impl(
                context = context,
                onStartGame = onConfirmSettings,
                onClose = onClose,
                analytics = analytics,
                dispatchers = dispatchers
            )
    }
}