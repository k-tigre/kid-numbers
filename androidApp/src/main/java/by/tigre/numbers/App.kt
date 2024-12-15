package by.tigre.numbers

import android.app.Application
import by.tigre.numbers.di.ApplicationGraph
import by.tigre.tools.logger.CrashlyticsLogger
import by.tigre.tools.logger.DbLogger
import by.tigre.tools.logger.Log
import by.tigre.tools.logger.LogcatLogger

class App : Application() {
    lateinit var graph: ApplicationGraph
        private set

    override fun onCreate() {
        super.onCreate()
        initLoggers()

        graph = ApplicationGraph.create(this)
    }

    private fun initLoggers() {
        val crashlyticsLogger = CrashlyticsLogger(BuildConfig.BUILD_TYPE)
        if (BuildConfig.DEBUG) {
            Log.init(Log.Level.VERBOSE, LogcatLogger(), crashlyticsLogger, DbLogger(this))
        } else {
            Log.init(Log.Level.DEBUG, crashlyticsLogger)
        }
    }
}
