package by.tigre.numbers.data.leaderboard

import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.LeaderboardEntry
import by.tigre.numbers.entity.LeaderboardSubmitRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreLeaderboardRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : LeaderboardRepository {

    override suspend fun addGameScore(request: LeaderboardSubmitRequest): Result<Unit> = runCatching {
        val documentId: String = LeaderboardDocumentId.forUser(request.userId, request.difficult)
        val documentRef = firestore.collection(COLLECTION).document(documentId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(documentRef)
            val gameFields: Map<String, Any> = request.toGameFields()
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

    override suspend fun fetchTopEntries(difficult: Difficult, limit: Int): Result<List<LeaderboardEntry>> = runCatching {
        firestore.collection(COLLECTION)
            .whereEqualTo(FIELD_DIFFICULTY, difficult.name)
            .orderBy(FIELD_SCORE, Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                runCatching { document.toEntry() }.getOrNull()
            }
    }

    private fun LeaderboardSubmitRequest.toGameFields(): Map<String, Any> = mapOf(
        FIELD_USER_ID to userId,
        FIELD_NICKNAME to nickname,
        FIELD_DIFFICULTY to difficult.name,
        FIELD_TIMESTAMP to timestampMillis,
        FIELD_SOLVE_TIME to solveTimeSeconds,
        FIELD_HINTS to hintsUsed,
        FIELD_MISTAKES to mistakes,
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toEntry(): LeaderboardEntry? {
        val parsedDifficult: Difficult = runCatching {
            Difficult.valueOf(getString(FIELD_DIFFICULTY) ?: return null)
        }.getOrNull() ?: return null
        return LeaderboardEntry(
            nickname = getString(FIELD_NICKNAME) ?: return null,
            totalScore = getLong(FIELD_SCORE)?.toInt() ?: return null,
            gamesCount = getLong(FIELD_GAMES_COUNT)?.toInt() ?: return null,
            difficult = parsedDifficult,
            timestampMillis = getLong(FIELD_TIMESTAMP) ?: return null,
        )
    }

    private companion object {
        const val COLLECTION: String = "leaderboard"
        const val FIELD_USER_ID: String = "userId"
        const val FIELD_NICKNAME: String = "nickname"
        const val FIELD_SCORE: String = "score"
        const val FIELD_GAMES_COUNT: String = "gamesCount"
        const val FIELD_SOLVE_TIME: String = "solveTimeSeconds"
        const val FIELD_HINTS: String = "hintsUsed"
        const val FIELD_MISTAKES: String = "mistakes"
        const val FIELD_DIFFICULTY: String = "difficulty"
        const val FIELD_TIMESTAMP: String = "timestamp"
    }
}
