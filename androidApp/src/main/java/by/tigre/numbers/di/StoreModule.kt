package by.tigre.numbers.di

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.adapter.primitive.IntColumnAdapter
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import by.tigre.numbers.core.data.storage.DatabaseNumbers
import by.tigre.numbers.data.challenges.ChallengeDurationAdapter
import by.tigre.numbers.data.challenges.ChallengeStatusAdapter
import by.tigre.numbers.data.challenges.ChallengesStore
import by.tigre.numbers.data.history.ResultStore
import by.tigre.numbers.data.history.StoreDifficultAdapter
import by.tigre.numbers.data.history.StoreGameTypeAdapter
import by.tigre.numbers.db.Challenges
import by.tigre.numbers.db.History
import by.tigre.tools.tools.coroutines.CoroutineModule

interface StoreModule {
    val resultStore: ResultStore
    val challengesStore: ChallengesStore

    class Impl(
        context: Context,
        coroutineModule: CoroutineModule,
        analyticsModule: AnalyticsModule
    ) : StoreModule {
        private val database: DatabaseNumbers by lazy {
            DatabaseNumbers(
                driver = AndroidSqliteDriver(
                    schema = DatabaseNumbers.Schema.synchronous(),
                    context = context,
                    name = "numbers.db",
                    callback = object : AndroidSqliteDriver.Callback(DatabaseNumbers.Schema.synchronous()) {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            db.setForeignKeyConstraintsEnabled(true)
                        }
                    }
                ),
                HistoryAdapter = History.Adapter(
                    difficultAdapter = StoreDifficultAdapter,
                    correctCountAdapter = IntColumnAdapter,
                    totalCountAdapter = IntColumnAdapter,
                    gameTypeAdapter = StoreGameTypeAdapter
                ),
                ChallengesAdapter = Challenges.Adapter(
                    statusAdapter = ChallengeStatusAdapter,
                    durationAdapter = ChallengeDurationAdapter
                )
            )
        }

        override val resultStore: ResultStore by lazy {
            ResultStore.Impl(database = database, scope = coroutineModule.scope, analytics = analyticsModule.eventAnalytics)
        }

        override val challengesStore: ChallengesStore by lazy {
            ChallengesStore.Impl(database = database, scope = coroutineModule.scope)
        }
    }
}
