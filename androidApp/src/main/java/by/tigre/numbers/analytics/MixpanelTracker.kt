package by.tigre.numbers.analytics

import android.content.Context
import by.tigre.numbers.BuildConfig
import by.tigre.tools.tools.coroutines.CoreScope
import by.tigre.tools.tools.coroutines.extensions.tickerFlow
import com.mixpanel.android.mpmetrics.MixpanelAPI
import kotlinx.coroutines.launch

class MixpanelTracker(context: Context, scope: CoreScope) : Tracker {
    private val mixpanel = MixpanelAPI.getInstance(context, BuildConfig.MiXPANEL_TOKEN, true)

    init {
        scope.launch {
            tickerFlow(PERIOD, DELAY)
                .collect { mixpanel.flush() }
        }
    }


    override fun trackEvent(event: Event.Action) {
        mixpanel.trackMap("ACTION:${event.name}", (event as? Event.WithPayload)?.payload)
    }

    override fun trackScreen(previous: Event.Screen?, current: Event.Screen) {
        mixpanel.trackMap(
            "SCREEN:${current.name}",
            mapOf("prev_screen" to previous?.name).run { if (current is Event.WithPayload) this + current.payload else this }
        )
    }

    private companion object {
        const val PERIOD = 5 * 60 * 1000L
        const val DELAY = 5000L
    }
}
