package by.tigre.numbers

import android.app.Application
import android.os.Process
import by.tigre.logger.CrashlyticsLogger
import by.tigre.logger.DbLogger
import by.tigre.logger.Log
import by.tigre.logger.LogDatabaseDriverFactory
import by.tigre.logger.LogcatLogger
import by.tigre.numbers.analytics.FirebaseTracker
import by.tigre.numbers.analytics.LogTracker
import by.tigre.numbers.analytics.MixpanelTracker
import by.tigre.numbers.analytics.Tracker
import by.tigre.numbers.di.ApplicationGraph

class App : Application() {
    lateinit var graph: ApplicationGraph
        private set

    override fun onCreate() {
        super.onCreate()
        initLoggers()

        graph = ApplicationGraph.create(
            context = this,
            tracker = { scope ->
                if (BuildConfig.REMOTE_ANALYTICS_ENABLED) {
                    Tracker.TrackerAggregator(
                        LogTracker(),
                        FirebaseTracker(this),
                        MixpanelTracker(this, scope = scope)
                    )
                } else {
                    Tracker.TrackerAggregator(
                        LogTracker(),
                    )
                }
            }
        )
    }

    private fun initLoggers() {
        val crashlyticsLogger = CrashlyticsLogger()
        if (BuildConfig.DEBUG) {
            Log.init(
                Log.Level.VERBOSE,
                LogcatLogger(),
                crashlyticsLogger,
                DbLogger(LogDatabaseDriverFactory.create(this), Process.myPid())
            )
        } else {
            Log.init(Log.Level.DEBUG, crashlyticsLogger)
        }
    }
}
