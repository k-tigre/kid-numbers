package by.tigre.numbers.extension

import by.tigre.numbers.analytics.Event
import by.tigre.numbers.analytics.ScreenAnalytics
import by.tigre.tools.logger.Log
import by.tigre.tools.logger.extensions.TAG_UNEXPECTED
import by.tigre.tools.logger.extensions.debugLog
import by.tigre.tools.presentation.base.toFlow
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.mapNotNull


suspend inline fun <reified T> Value<ChildStack<*, *>>.trackScreens(
    analytics: ScreenAnalytics,
    name: String,
    crossinline screenMapper: (T) -> Event.Screen
) {
    toFlow()
        .debugLog("trackScreens - ${name}")
        .mapNotNull {
            (it.active.configuration as? T)?.let(screenMapper)
                ?: run {
                    Log.e(TAG_UNEXPECTED) { "handleUntrackedScreenConfig: ${it.active.configuration::class.java.name}" }
                    null
                }
        }
        .collect(analytics::trackScreen)
}