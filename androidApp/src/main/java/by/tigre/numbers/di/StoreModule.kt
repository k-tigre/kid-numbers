package by.tigre.numbers.di

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.adapter.primitive.IntColumnAdapter
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
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
            fun migrate5(driver: SqlDriver) {
                data class TempResult(
                    val correctCount: Long,
                    val totalCount: Long
                )

                val result = driver.executeQuery(
                    identifier = null,
                    sql = "SELECT * FROM HISTORY WHERE HISTORY.challengeId IS NOT NULL;",
                    mapper = { cursor ->
                        val results = mutableMapOf<String, MutableList<TempResult>>()
                        while (cursor.next().value) {
                            val challengeId = cursor.getString(7) ?: continue
                            val list = results[challengeId] ?: mutableListOf()
                            list.add(
                                TempResult(
                                    correctCount = cursor.getLong(4) ?: 0,
                                    totalCount = cursor.getLong(5) ?: 0
                                )
                            )
                            results[challengeId] = list
                        }

                        QueryResult.Value(results)
                    },
                    parameters = 0
                )

                result.value.forEach { id, result ->
                    val isSuccess = result.all { it.totalCount == it.correctCount }
                    driver.execute(
                        identifier = null,
                        sql = "UPDATE Challenges SET isSuccess=? WHERE Challenges.id=?",
                        binders = {
                            bindLong(0, if (isSuccess) 1 else 0)
                            bindString(1, id)
                        },
                        parameters = 2
                    )
                }
            }

            DatabaseNumbers(
                driver = AndroidSqliteDriver(
                    schema = DatabaseNumbers.Schema.synchronous(),
                    context = context,
                    name = "numbers.db",
                    callback = object : AndroidSqliteDriver.Callback(
                        schema = DatabaseNumbers.Schema.synchronous(),
                        AfterVersion(5, ::migrate5),
                    ) {
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
