package by.tigre.numbers.domain.facts

import by.tigre.numbers.entity.ErrorType
import by.tigre.numbers.entity.FactKey
import kotlin.math.abs

object ErrorClassifier {
    fun classify(key: FactKey, given: Int, correct: Int): ErrorType {
        if (isWrongOperation(key, given)) return ErrorType.WRONG_OPERATION
        if (isNeighborFact(key, given)) return ErrorType.NEIGHBOR_FACT
        if (abs(given - correct) == 1) return ErrorType.OFF_BY_ONE
        if (abs(given - correct) == 10) return ErrorType.OFF_BY_TEN
        if (isSignError(key, given, correct)) return ErrorType.SIGN_ERROR
        if (isCarryError(key, given)) return ErrorType.CARRY_ERROR
        if (isBorrowError(key, given, correct)) return ErrorType.BORROW_ERROR
        return ErrorType.RANDOM
    }

    private fun isWrongOperation(key: FactKey, given: Int): Boolean {
        val a: Int = key.a
        val b: Int = key.b
        return when (key.op) {
            FactKey.Op.MUL -> given == a + b
            FactKey.Op.DIV -> given == a - b
            FactKey.Op.ADD -> a <= 12 && b <= 12 && given == a * b
            FactKey.Op.SUB -> given == a + b
        }
    }

    private fun isNeighborFact(key: FactKey, given: Int): Boolean {
        if (key.op != FactKey.Op.MUL) return false
        val a: Int = key.a
        val b: Int = key.b
        return listOf(
            (a - 1) * b,
            (a + 1) * b,
            a * (b - 1),
            a * (b + 1),
            (a - 1) * (b + 1),
            (a + 1) * (b - 1),
        ).any { it == given }
    }

    private fun isSignError(key: FactKey, given: Int, correct: Int): Boolean {
        if (key.op != FactKey.Op.SUB) return false
        return given == key.b - key.a || given == -correct
    }

    private fun isCarryError(key: FactKey, given: Int): Boolean {
        if (key.op != FactKey.Op.ADD) return false
        val a: Int = key.a
        val b: Int = key.b
        if (a % 10 + b % 10 < 10) return false
        val withoutCarry: Int = a / 10 * 10 + b / 10 * 10 + (a % 10 + b % 10) % 10
        return given == withoutCarry
    }

    private fun isBorrowError(key: FactKey, given: Int, correct: Int): Boolean {
        if (key.op != FactKey.Op.SUB) return false
        val a: Int = key.a
        val b: Int = key.b
        if (a % 10 >= b % 10) return false
        return given == correct + 10
    }
}
