package by.tigre.numbers.domain.facts

import by.tigre.numbers.core.data.storage.DatabaseNumbers
import by.tigre.numbers.data.facts.FactBackfillPreferences
import by.tigre.numbers.data.facts.FactStore
import by.tigre.numbers.data.history.ResultStore
import by.tigre.numbers.entity.GameResult
import kotlinx.serialization.json.Json

class FactBackfill(
    private val loadSessions: suspend () -> List<SessionRow>,
    private val factStore: FactStore,
    private val preferences: FactBackfillPreferences,
    private val json: Json = Json,
) {

    suspend fun runIfNeeded() {
        runCatching {
            if (preferences.isDone()) return@runCatching
            backfillSessions(loadSessions())
            preferences.markDone()
        }
    }

    internal suspend fun backfillSessions(sessions: List<SessionRow>) {
        sessions.sortedBy { it.id }.forEach { session ->
            runCatching {
                val result: GameResult = json.decodeFromString(GameResult.serializer(), session.data)
                ResultStore.applyFactUpdates(result.results, factStore, session.date)
            }
        }
    }

    data class SessionRow(
        val id: Long,
        val date: Long,
        val data: String,
    )

    companion object {
        const val MAX_SESSIONS: Long = 200L

        fun create(
            database: DatabaseNumbers,
            factStore: FactStore,
            preferences: FactBackfillPreferences,
        ): FactBackfill = FactBackfill(
            loadSessions = {
                database.historyItemsQueries.selectRecentWithData(limit = MAX_SESSIONS)
                    .executeAsList()
                    .map { row ->
                        SessionRow(id = row.id, date = row.date, data = row.data_)
                    }
            },
            factStore = factStore,
            preferences = preferences,
        )
    }
}
