package by.tigre.tools.logger

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.setCustomKeys

class CrashlyticsLogger(type: String) : Log.Logger {
    init {
        FirebaseCrashlytics.getInstance().setCustomKeys {
            key("app_type", type)
        }
    }

    override fun log(level: Log.Level, vararg fields: Pair<String, String>) {
        if (level >= Log.Level.INFO) {
            val message = fields
                .filter { (key, _) ->
                    key != Log.Field.TIMESTAMP && key != Log.Field.STACKTRACE
                }
                .joinToString(
                    separator = ", ",
                    prefix = "$level; "
                ) { (key, value) -> "$key:$value" }
            FirebaseCrashlytics.getInstance().log(message)
        }
    }
}
