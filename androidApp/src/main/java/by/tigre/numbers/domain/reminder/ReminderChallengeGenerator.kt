package by.tigre.numbers.domain.reminder

import by.tigre.numbers.data.challenges.ChallengesStore
import by.tigre.numbers.data.history.ResultStore
import by.tigre.numbers.entity.Challenge
import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameSettings
import by.tigre.numbers.entity.GameType
import kotlinx.coroutines.flow.first
import kotlin.random.Random
import kotlin.time.ExperimentalTime


interface ReminderChallengeGenerator {

    suspend fun generateIfNeeded(): String?

    @OptIn(ExperimentalTime::class)
    class Impl(
        private val challengesStore: ChallengesStore,
        private val resultStore: ResultStore
    ) : ReminderChallengeGenerator {

        override suspend fun generateIfNeeded(): String? {
            val hasChallenge = challengesStore.hasChallenges.first()
            if (hasChallenge) {
                return null
            } else {
                val tasks = generateTasks()
                val challenge = Challenge(
                    id = Challenge.NO_ID,
                    tasks = tasks,
                    duration = Challenge.Duration.OneDay,
                    status = Challenge.Status.New,
                    startDate = -1,
                    endDate = -1,
                    isSuccess = false
                )
                val id = challengesStore.add(challenge)
                return id
            }
        }

        private suspend fun generateTasks(): List<Challenge.Task> {
            val result = resultStore.load(
                difficult = Difficult.entries,
                onlySuccess = false,
                types = GameType.entries,
                limit = 100
            )


            val referenceTask = (result.filter { it.difficult == Difficult.Hard }.randomOrNull()
                ?: result.filter { it.difficult == Difficult.Medium }.randomOrNull()
                ?: result.filter { it.difficult == Difficult.Easy }.randomOrNull())
                ?.let { (id, _, _, _, _, _, _) -> resultStore.getDetails(id) }

            return if (referenceTask == null || referenceTask.results.isEmpty()) {
                listOf(
                    Challenge.Task(
                        id = -1,
                        gameSettings = GameSettings.Additional(
                            range = GameSettings.Range(max = 20, withNegative = Random.nextBoolean()),
                            difficult = Difficult.Easy,
                            isPositive = Random.nextBoolean()
                        ),
                        isCompleted = false
                    ),
                    Challenge.Task(
                        id = -1,
                        gameSettings = GameSettings.Additional(
                            range = GameSettings.Range(max = 20, withNegative = Random.nextBoolean()),
                            difficult = Difficult.Easy,
                            isPositive = Random.nextBoolean()
                        ),
                        isCompleted = false
                    )
                )
            } else {
                (0..2).map {
                    when (referenceTask.type) {
                        GameType.Additional,
                        GameType.Subtraction -> GameSettings.Additional(
                            range = GameSettings.Range(max = 20, withNegative = Random.nextBoolean()),
                            difficult = referenceTask.difficult,
                            isPositive = Random.nextBoolean()
                        )

                        GameType.Multiplication -> GameSettings.Multiplication(
                            selectedNumbers = listOf(
                                Random.nextInt(1, 9),
                                Random.nextInt(1, 9)
                            ),
                            difficult = referenceTask.difficult,
                            isPositive = true
                        )

                        GameType.Division -> GameSettings.Multiplication(
                            selectedNumbers = listOf(
                                Random.nextInt(1, 9),
                                Random.nextInt(1, 9)
                            ),
                            difficult = referenceTask.difficult,
                            isPositive = false
                        )

                        GameType.Equations -> GameSettings.Equations(
                            range = GameSettings.Range(max = 20, withNegative = Random.nextBoolean()),
                            difficult = referenceTask.difficult,
                            dimension = GameSettings.Equations.Dimension.Single,
                            type = GameSettings.Equations.Type.entries.random()
                        )
                    }
                }.map { gameSettings ->
                    Challenge.Task(
                        id = -1,
                        gameSettings = gameSettings,
                        isCompleted = false
                    )
                }
            }

        }
    }
}