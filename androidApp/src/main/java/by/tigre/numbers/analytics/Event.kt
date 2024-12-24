package by.tigre.numbers.analytics

import by.tigre.numbers.entity.Difficult
import by.tigre.numbers.entity.GameType

sealed class Event(val name: String) {

    sealed class Action(name: String) : Event(name) {
        sealed class UI(name: String) : Action("UI_$name") {
            sealed class Button(name: String) : UI("${name}_clicked")
        }

        sealed class Logic(name: String) : Action("Logic_$name") {
            data object RandomBigDeep : Logic("RandomBigDeep")
        }
    }

    interface WithPayload {
        val payload: Map<String, String>
    }

    sealed class Screen(name: String) : Event(name) {
        data object MainMenu : Screen("MainMenu")
        data object History : Screen("History")
        data class RootGame(private val type: GameType) : Screen("RootGame"), WithPayload {
            override val payload: Map<String, String> by lazy { mapOf("type" to type.name) }
        }

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
            private val inCorrectCount: Int,
            private val totalCount: Int,
            private val difficult: Difficult,
            private val type: GameType,
        ) : Screen("GameResult"), WithPayload {
            override val payload: Map<String, String> by lazy {
                mapOf(
                    "difficult" to difficult.name,
                    "totalCount" to totalCount.toString(),
                    "inCorrectCount" to inCorrectCount.toString(),
                    "correctCount" to correctCount.toString(),
                    "gameType" to type.toString()
                )
            }
        }
    }
}
