package by.tigre.numbers.di

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.adapter.primitive.FloatColumnAdapter
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
import by.tigre.numbers.data.facts.FactBackfillPreferences
import by.tigre.numbers.data.facts.FactBackfillPreferencesImpl
import by.tigre.numbers.data.facts.FactStore
import by.tigre.numbers.data.facts.SmartPracticePreferences
import by.tigre.numbers.data.facts.SmartPracticePreferencesImpl
import by.tigre.numbers.data.history.ResultStore
import by.tigre.numbers.domain.facts.FactBackfill
import kotlinx.coroutines.launch
import by.tigre.numbers.data.history.StoreDifficultAdapter
import by.tigre.numbers.data.history.StoreGameTypeAdapter
import by.tigre.numbers.data.storage.Preferences
import by.tigre.numbers.domain.OnboardingRepository
import by.tigre.numbers.domain.OnboardingRepositoryImpl
import by.tigre.numbers.db.Challenges
import by.tigre.numbers.db.Fact
import by.tigre.numbers.db.History
import by.tigre.tools.tools.coroutines.CoroutineModule

interface StoreModule {
    val resultStore: ResultStore
    val factStore: FactStore
    val challengesStore: ChallengesStore
    val preferences: Preferences
    val smartPracticePreferences: SmartPracticePreferences
    val onboardingRepository: OnboardingRepository

    class Impl(
        context: Context,
        coroutineModule: CoroutineModule,
        analyticsModule: AnalyticsModule
    ) : StoreModule {
        override val preferences: Preferences = Preferences.Impl(context, "main")
        override val smartPracticePreferences: SmartPracticePreferences = SmartPracticePreferencesImpl(preferences)
        override val onboardingRepository: OnboardingRepository = OnboardingRepositoryImpl(preferences)

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
                ),
                FactAdapter = Fact.Adapter(
                    aAdapter = IntColumnAdapter,
                    bAdapter = IntColumnAdapter,
                    pAdapter = FloatColumnAdapter,
                    avgTimeMsAdapter = IntColumnAdapter,
                    attemptsAdapter = IntColumnAdapter,
                    correctAdapter = IntColumnAdapter,
                    streakAdapter = IntColumnAdapter,
                    lastSeenMsAdapter = StoreLongAdapter,
                    intervalDaysAdapter = IntColumnAdapter,
                    dueAtMsAdapter = StoreLongAdapter
                )
            )
        }

        override val resultStore: ResultStore by lazy {
            ResultStore.Impl(
                database = database,
                scope = coroutineModule.scope,
                analytics = analyticsModule.eventAnalytics,
                factStore = factStore,
            )
        }

        override val factStore: FactStore by lazy {
            FactStore.Impl(database = database)
        }

        override val challengesStore: ChallengesStore by lazy {
            ChallengesStore.Impl(database = database, scope = coroutineModule.scope)
        }

        private val factBackfillPreferences: FactBackfillPreferences = FactBackfillPreferencesImpl(preferences)
        private val factBackfill: FactBackfill by lazy {
            FactBackfill.create(
                database = database,
                factStore = factStore,
                preferences = factBackfillPreferences,
            )
        }

        init {
            coroutineModule.scope.launch(coroutineModule.dispatchers.io) {
                factBackfill.runIfNeeded()
            }
        }
    }
}

private object StoreLongAdapter : ColumnAdapter<Long, Long> {
    override fun decode(databaseValue: Long): Long = databaseValue
    override fun encode(value: Long): Long = value
}
