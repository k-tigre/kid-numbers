package by.tigre.numbers.data.leaderboard

import by.tigre.numbers.entity.Difficult

object LeaderboardDocumentId {

    fun forUser(userId: String, difficult: Difficult): String = "${difficult.name}_$userId"
}
