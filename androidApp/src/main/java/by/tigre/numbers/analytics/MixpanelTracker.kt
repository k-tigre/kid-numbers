package by.tigre.numbers.analytics

import android.content.Context
import by.tigre.numbers.BuildConfig
import com.mixpanel.android.mpmetrics.MixpanelAPI

class MixpanelTracker(context: Context) : Tracker {
    private val mixpanel = MixpanelAPI.getInstance(context, BuildConfig.MiXPANEL_TOKEN, true)

    override fun trackEvent(event: Event.Action) {
        mixpanel.trackMap("ACTION:${event.name}", (event as? Event.WithPayload)?.payload)
    }

    override fun trackScreen(previous: Event.Screen?, current: Event.Screen) {
        mixpanel.trackMap(
            "SCREEN:${current.name}",
            mapOf("prev_screen" to previous?.name).run { if (current is Event.WithPayload) this + current.payload else this }
        )
    }
}
