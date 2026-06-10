package by.tigre.numbers.data.leaderboard

import by.tigre.numbers.entity.LeaderboardEntry
import by.tigre.numbers.entity.LeaderboardSpeedEntry
import by.tigre.numbers.entity.LeaderboardSpeedSubmit
import by.tigre.numbers.entity.LeaderboardSubmitRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreLeaderboardRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : LeaderboardRepository {

    override suspend fun submitSpeedRecord(request: LeaderboardSpeedSubmit): Result<Unit> = runCatching {
        val documentId: String = LeaderboardBoardKey.documentId(request.boardKey, request.userId)
        val documentRef = firestore.collection(COLLECTION_SPEED).document(documentId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(documentRef)
            val fields: Map<String, Any> = request.toSpeedFields()
            if (!snapshot.exists()) {
                transaction.set(
                    documentRef,
                    fields + mapOf(FIELD_BEST_TIME to request.solveTimeSeconds),
                )
            } else {
                val currentBest: Long = snapshot.getLong(FIELD_BEST_TIME) ?: Long.MAX_VALUE
                val currentMistakes: Long = snapshot.getLong(FIELD_MISTAKES) ?: Long.MAX_VALUE
                val isBetter: Boolean = request.solveTimeSeconds < currentBest ||
                    (request.solveTimeSeconds == currentBest && request.mistakes < currentMistakes)
                if (isBetter) {
                    transaction.set(
                        documentRef,
                        fields + mapOf(FIELD_BEST_TIME to request.solveTimeSeconds),
                        SetOptions.merge(),
                    )
                }
            }
            null
        }.await()
        Unit
    }

    override suspend fun addTotalScore(request: LeaderboardSubmitRequest): Result<Unit> = runCatching {
        val documentRef = firestore.collection(COLLECTION_TOTAL).document(request.userId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(documentRef)
            val gameFields: Map<String, Any> = request.toTotalFields()
            if (snapshot.exists()) {
                val currentScore: Long = snapshot.getLong(FIELD_SCORE) ?: 0L
                val currentGames: Long = snapshot.getLong(FIELD_GAMES_COUNT) ?: 0L
                transaction.set(
                    documentRef,
                    gameFields + mapOf(
                        FIELD_SCORE to currentScore + request.gameScore,
                        FIELD_GAMES_COUNT to currentGames + 1L,
                    ),
                    SetOptions.merge(),
                )
            } else {
                transaction.set(
                    documentRef,
                    gameFields + mapOf(
                        FIELD_SCORE to request.gameScore.toLong(),
                        FIELD_GAMES_COUNT to 1L,
                    ),
                )
            }
            null
        }.await()
        Unit
    }

    override suspend fun fetchBestSpeedEntry(boardKey: String): Result<LeaderboardSpeedEntry?> = runCatching {
        firestore.collection(COLLECTION_SPEED)
            .whereEqualTo(FIELD_BOARD_KEY, boardKey)
            .orderBy(FIELD_BEST_TIME, Query.Direction.ASCENDING)
            .limit(1L)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toSpeedEntry()
    }

    override suspend fun fetchTopSpeedEntries(boardKey: String, limit: Int): Result<List<LeaderboardSpeedEntry>> = runCatching {
        firestore.collection(COLLECTION_SPEED)
            .whereEqualTo(FIELD_BOARD_KEY, boardKey)
            .orderBy(FIELD_BEST_TIME, Query.Direction.ASCENDING)
            .limit(limit.toLong())
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                runCatching { document.toSpeedEntry() }.getOrNull()
            }
    }

    override suspend fun fetchTopTotalEntries(limit: Int): Result<List<LeaderboardEntry>> = runCatching {
        firestore.collection(COLLECTION_TOTAL)
            .orderBy(FIELD_SCORE, Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                runCatching { document.toTotalEntry() }.getOrNull()
            }
    }

    private fun LeaderboardSpeedSubmit.toSpeedFields(): Map<String, Any> = mapOf(
        FIELD_USER_ID to userId,
        FIELD_NICKNAME to nickname,
        FIELD_BOARD_KEY to boardKey,
        FIELD_GAME_TYPE to gameType.name,
        FIELD_DIFFICULTY to difficult.name,
        FIELD_TASK_COUNT to taskCount,
        FIELD_SOLVE_TIME to solveTimeSeconds,
        FIELD_MISTAKES to mistakes,
        FIELD_TIMESTAMP to timestampMillis,
    )

    private fun LeaderboardSubmitRequest.toTotalFields(): Map<String, Any> = mapOf(
        FIELD_USER_ID to userId,
        FIELD_NICKNAME to nickname,
        FIELD_TIMESTAMP to timestampMillis,
        FIELD_SOLVE_TIME to solveTimeSeconds,
        FIELD_HINTS to hintsUsed,
        FIELD_MISTAKES to mistakes,
        FIELD_TIME_CAP to timeCapSeconds,
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toSpeedEntry(): LeaderboardSpeedEntry? {
        return LeaderboardSpeedEntry(
            nickname = getString(FIELD_NICKNAME) ?: return null,
            bestTimeSeconds = getLong(FIELD_BEST_TIME) ?: return null,
            mistakes = getLong(FIELD_MISTAKES)?.toInt() ?: return null,
            timestampMillis = getLong(FIELD_TIMESTAMP) ?: return null,
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toTotalEntry(): LeaderboardEntry? {
        return LeaderboardEntry(
            nickname = getString(FIELD_NICKNAME) ?: return null,
            totalScore = getLong(FIELD_SCORE)?.toInt() ?: return null,
            gamesCount = getLong(FIELD_GAMES_COUNT)?.toInt() ?: return null,
            timestampMillis = getLong(FIELD_TIMESTAMP) ?: return null,
        )
    }

    private companion object {
        const val COLLECTION_SPEED: String = "leaderboard_speed"
        const val COLLECTION_TOTAL: String = "leaderboard_total"
        const val FIELD_USER_ID: String = "userId"
        const val FIELD_NICKNAME: String = "nickname"
        const val FIELD_BOARD_KEY: String = "boardKey"
        const val FIELD_GAME_TYPE: String = "gameType"
        const val FIELD_DIFFICULTY: String = "difficulty"
        const val FIELD_TASK_COUNT: String = "taskCount"
        const val FIELD_BEST_TIME: String = "bestTimeSeconds"
        const val FIELD_SCORE: String = "score"
        const val FIELD_GAMES_COUNT: String = "gamesCount"
        const val FIELD_SOLVE_TIME: String = "solveTimeSeconds"
        const val FIELD_HINTS: String = "hintsUsed"
        const val FIELD_MISTAKES: String = "mistakes"
        const val FIELD_TIME_CAP: String = "timeCapSeconds"
        const val FIELD_TIMESTAMP: String = "timestamp"
    }
}
