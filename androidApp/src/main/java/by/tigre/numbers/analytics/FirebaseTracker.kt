package by.tigre.numbers.analytics

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent

class FirebaseTracker(context: Context) : Tracker {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    override fun trackEvent(event: Event.Action) {
        if (event is Event.WithPayload) {
            firebaseAnalytics.logEvent("ACTION_${event.name}") {
                event.payload.forEach { (key, value) ->
                    param(key, value)
                }
            }
        } else {
            firebaseAnalytics.logEvent(event.name, null)
        }
    }

    override fun trackScreen(previous: Event.Screen?, current: Event.Screen) {
        firebaseAnalytics.logEvent("SCREEN_${current.name}") {
            param(KEY, previous?.name.toString())
            if (current is Event.WithPayload) {
                current.payload.forEach { (key, value) ->
                    param(key, value)
                }
            }
        }

        // TODO remove?
        firebaseAnalytics.logEvent("screen_view") {
            param(KEY, previous?.name.toString())
            param("firebase_screen_class", current.name)
            if (current is Event.WithPayload) {
                current.payload.forEach { (key, value) ->
                    param(key, value)
                }
            }
        }
    }

    private companion object {
        const val KEY = "prev_screen"
    }
}