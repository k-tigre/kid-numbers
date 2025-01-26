package by.tigre.numbers.data.challenges

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import by.tigre.numbers.core.data.storage.DatabaseNumbers
import by.tigre.numbers.entity.Challenge
import by.tigre.numbers.entity.ChallengeWithCount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface ChallengesStore {
    suspend fun add(challenge: Challenge)
    suspend fun remove(id: String)

    val challenges: Flow<List<ChallengeWithCount>>
    val hasActiveChallenge: Flow<Boolean>
    suspend fun getChallenge(id: String): Challenge?
    suspend fun start(id: String)
    suspend fun setTaskCompleted(id: Long)
    suspend fun setChallengeCompleted(id: String)
    suspend fun getNextTask(challengeId: String): Challenge.Task?

    class Impl(
        private val database: DatabaseNumbers,
        private val scope: CoroutineScope,
    ) : ChallengesStore {

        private val json = Json

        override val challenges: Flow<List<ChallengeWithCount>> = database.challengesQueries
            .getChallengesWithCountNotFinished()
            .asFlow()
            .mapToList(scope.coroutineContext)
            .map { result ->
                result.map { item ->
                    ChallengeWithCount(
                        id = item.id,
                        taskCount = item.tasksCount.toInt(),
                        startDate = item.startDate ?: -1,
                        status = item.status,
                        duration = item.duration
                    )
                }
            }
            .shareIn(scope, started = SharingStarted.WhileSubscribed(), replay = 1)

        override val hasActiveChallenge: Flow<Boolean> = challenges
            .map { it.any { challenge -> challenge.status == Challenge.Status.Active } }

        @OptIn(ExperimentalUuidApi::class)
        override suspend fun add(challenge: Challenge) {
            database.transaction {
                if (challenge.id != Challenge.NO_ID) {
                    database.challengesQueries.removeChallenge(challenge.id)
                }

                val id = Uuid.random().toHexString()
                database.challengesQueries.addChallenge(
                    id = id,
                    date = System.currentTimeMillis(),
                    duration = challenge.duration,
                    status = challenge.status
                )

                challenge.tasks.forEach { task ->
                    database.challengesQueries.addChallengeTask(
                        settings = json.encodeToString(task.gameSettings),
                        isCompleted = false,
                        challengesId = id
                    )
                }
            }
        }

        override suspend fun remove(id: String) {
            database.challengesQueries.removeChallenge(id)
        }

        override suspend fun getChallenge(id: String): Challenge? {
            val challenge = database.challengesQueries.getChallenge(id).executeAsOneOrNull() ?: return null
            val tasks = database.challengesQueries.getChallengeTasks(id).executeAsList()

            return Challenge(
                id = challenge.id,
                duration = challenge.duration,
                tasks = tasks.map {
                    Challenge.Task(
                        gameSettings = json.decodeFromString(it.settings),
                        id = it.id,
                        isCompleted = it.isCompleted
                    )
                },
                startDate = challenge.startDate ?: -1,
                endDate = challenge.endDate ?: -1,
                status = challenge.status
            )
        }

        override suspend fun start(id: String) {
            database.challengesQueries.startChallenge(startDate = System.currentTimeMillis(), id = id)
        }

        override suspend fun setTaskCompleted(id: Long) {
            database.challengesQueries.updateChallengeTask(isCompleted = true, id = id)
        }

        override suspend fun setChallengeCompleted(id: String) {
            database.challengesQueries.finishChallenge(id = id, endDate = System.currentTimeMillis())
        }

        override suspend fun getNextTask(challengeId: String): Challenge.Task? {
            return database.challengesQueries.getNonCompletedChallengeTask(challengeId).executeAsOneOrNull()?.let {
                Challenge.Task(
                    gameSettings = json.decodeFromString(it.settings),
                    id = it.id,
                    isCompleted = it.isCompleted
                )
            }
        }
    }
}