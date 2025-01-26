package by.tigre.numbers.analytics

import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameType

sealed class Event(val name: String) {

    sealed class Action(name: String) : Event(name) {
        sealed class UI(name: String) : Action("UI_$name") {
            sealed class Button(name: String) : UI("${name}_clicked")
            data class SettingScroll(val type: GameType) : UI("SettingScroll"), WithPayload {
                override val payload: Map<String, String> by lazy { mapOf("type" to type.name) }
            }
        }

        sealed class Logic(name: String) : Action("Logic_$name") {
            data object RandomBigDeep : Logic("RandomBigDeep")
            data object WrongCountInDB : Logic("WrongCountInDB")
            data object WrongChallengeInDB : Logic("WrongChallengeInDB")
            data object CanNotFindChallengeInDB : Logic("CanNotFindChallengeInDB")
            data class Error(val type: String) : Logic("Error"), WithPayload {
                override val payload: Map<String, String> by lazy { mapOf("type" to type) }
            }

            data class GenerateQuestions(
                private val duration: Long,
                private val difficult: Difficult,
                private val type: GameType,
            ) : Logic("GenerateQuestions"), WithPayload {
                override val payload: Map<String, String> by lazy {
                    mapOf(
                        "difficult" to difficult.name,
                        "duration" to duration.toString(),
                        "gameType" to type.toString()
                    )
                }
            }
        }
    }

    interface WithPayload {
        val payload: Map<String, String>
    }

    sealed class Screen(name: String, val skip: Boolean = false) : Event(name) {
        data object MainMenu : Screen("MainMenu")
        data object History : Screen("History")
        data object RootGame : Screen("RootGame", skip = true)
        data object RootChallenge : Screen("RootChallenge", skip = true)
        data object RootGameChallenge : Screen("RootGameChallenge", skip = true)

        data class GameSettings(private val type: GameType) : Screen("GameSettings"), WithPayload {
            override val payload: Map<String, String> by lazy { mapOf("type" to type.name) }
        }

        data class Game(
            private val difficult: Difficult,
        ) : Screen("Game"), WithPayload {
            override val payload: Map<String, String> by lazy { mapOf("difficult" to difficult.name) }
        }

        data class GameResult(
            private val correctCount: Int,
            private val incorrectCount: Int,
            private val totalCount: Int,
            private val difficult: Difficult,
            private val type: GameType,
        ) : Screen("GameResult"), WithPayload {
            override val payload: Map<String, String> by lazy {
                mapOf(
                    "difficult" to difficult.name,
                    "totalCount" to totalCount.toString(),
                    "incorrectCount" to incorrectCount.toString(),
                    "correctCount" to correctCount.toString(),
                    "success" to (incorrectCount == 0).toString(),
                    "gameType" to type.toString()
                )
            }
        }

        data object ChallengeResult : Screen("ChallengeResult"), WithPayload {
            override val payload: Map<String, String> by lazy {
                mapOf(
                )
            }
        }
    }
}
