package by.tigre.numbers.entity

import kotlinx.serialization.Serializable

data class FactKey(val op: Op, val a: Int, val b: Int) {
    @Serializable
    enum class Op { ADD, SUB, MUL, DIV }
    fun toStorageKey(): String = "${op.name}:$a:$b"
    companion object {
        fun parse(storageKey: String): FactKey {
            val parts: List<String> = storageKey.split(":")
            require(parts.size == 3) { "Invalid storage key: $storageKey" }
            val op: Op = Op.valueOf(parts[0])
            val a: Int = parts[1].toInt()
            val b: Int = parts[2].toInt()
            return FactKey(op, a, b)
        }
        fun fromQuestion(question: GameOptions.Question.Operation): FactKey? {
            return when (question) {
                is GameOptions.Question.Operation.Additional -> {
                    val (first: Int, second: Int) = if (question.a <= question.b) {
                        question.a to question.b
                    } else {
                        question.b to question.a
                    }
                    FactKey(Op.ADD, first, second)
                }
                is GameOptions.Question.Operation.Subtraction -> {
                    val minuend: Int = question.x + question.b
                    FactKey(Op.SUB, minuend, question.b)
                }
                is GameOptions.Question.Operation.Multiplication -> {
                    val (first: Int, second: Int) = if (question.first <= question.second) {
                        question.first to question.second
                    } else {
                        question.second to question.first
                    }
                    FactKey(Op.MUL, first, second)
                }
                is GameOptions.Question.Operation.Division -> {
                    val dividend: Int = question.x * question.second
                    FactKey(Op.DIV, dividend, question.second)
                }
            }
        }
    }
}
