package by.tigre.numbers.analytics

import by.tigre.tools.tools.coroutines.CoreDispatchers
import by.tigre.tools.tools.coroutines.CoreScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch


interface ScreenAnalytics {
    fun trackScreen(screen: Event.Screen)

    class Impl(
        private val tracker: Tracker,
        private val dispatchers: CoreDispatchers,
        private val scope: CoreScope
    ) : ScreenAnalytics {
        private val screens = MutableSharedFlow<Event.Screen>()

        init {
            scope.launch {
                screens
                    .distinctUntilChanged()
                    .scan((null to null) as Pair<Event.Screen?, Event.Screen?>) { previous, current ->
                        if (current.skip) {
                            previous
                        } else {
                            previous.second to current
                        }
                    }
                    .drop(1)
                    .distinctUntilChanged()
                    .flowOn(dispatchers.io)
                    .collect { (prev, current) ->
                        if (current != null)
                            tracker.trackScreen(previous = prev, current = current)
                    }
            }
        }

        override fun trackScreen(screen: Event.Screen) {
            scope.launch { screens.emit(screen) }
        }
    }
}
