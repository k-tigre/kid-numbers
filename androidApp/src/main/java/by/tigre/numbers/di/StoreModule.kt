package by.tigre.numbers.di

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.adapter.primitive.IntColumnAdapter
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import by.tigre.numbers.core.data.storage.DatabaseNumbers
import by.tigre.numbers.data.ResultStore
import by.tigre.numbers.data.history.StoreDifficultAdapter
import by.tigre.numbers.db.History
import by.tigre.tools.tools.coroutines.CoroutineModule

interface StoreModule {
    val resultStore: ResultStore

    class Impl(
        context: Context,
        coroutineModule: CoroutineModule
    ) : StoreModule {
        private val database: DatabaseNumbers by lazy {
            DatabaseNumbers(
                driver = AndroidSqliteDriver(
                    schema = DatabaseNumbers.Schema.synchronous(),
                    context = context,
                    name = "numbers.db",
                    callback = object : AndroidSqliteDriver.Callback(DatabaseNumbers.Schema.synchronous()) {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            db.execSQL("PRAGMA foreign_keys=ON;")
                        }
                    }
                ),
                HistoryAdapter = History.Adapter(
                    difficultAdapter = StoreDifficultAdapter,
                    correctCountAdapter = IntColumnAdapter,
                    totalCountAdapter = IntColumnAdapter
                )
            )
        }

        override val resultStore: ResultStore by lazy {
            ResultStore.Impl(database, coroutineModule.scope)
        }

    }
}
