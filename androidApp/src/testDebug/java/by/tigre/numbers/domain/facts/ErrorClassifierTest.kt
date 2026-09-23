package by.tigre.numbers.domain.facts

import by.tigre.numbers.entity.ErrorType
import by.tigre.numbers.entity.FactKey
import org.junit.Assert.assertEquals
import org.junit.Test

class ErrorClassifierTest {

    @Test
    fun mulWrongOperationUsesAddition() {
        val key: FactKey = FactKey(FactKey.Op.MUL, 3, 4)
        assertEquals(ErrorType.WRONG_OPERATION, ErrorClassifier.classify(key, given = 7, correct = 12))
    }

    @Test
    fun addWrongOperationUsesMultiplicationWhenSmall() {
        val key: FactKey = FactKey(FactKey.Op.ADD, 3, 4)
        assertEquals(ErrorType.WRONG_OPERATION, ErrorClassifier.classify(key, given = 12, correct = 7))
    }

    @Test
    fun divWrongOperationUsesSubtraction() {
        val key: FactKey = FactKey(FactKey.Op.DIV, 20, 4)
        assertEquals(ErrorType.WRONG_OPERATION, ErrorClassifier.classify(key, given = 16, correct = 5))
    }

    @Test
    fun subWrongOperationUsesAddition() {
        val key: FactKey = FactKey(FactKey.Op.SUB, 12, 5)
        assertEquals(ErrorType.WRONG_OPERATION, ErrorClassifier.classify(key, given = 17, correct = 7))
    }

    @Test
    fun mulNeighborFactAdjacentOperand() {
        val key: FactKey = FactKey(FactKey.Op.MUL, 3, 4)
        assertEquals(ErrorType.NEIGHBOR_FACT, ErrorClassifier.classify(key, given = 15, correct = 12))
    }

    @Test
    fun mulNeighborFactSwappedDelta() {
        val key: FactKey = FactKey(FactKey.Op.MUL, 3, 4)
        assertEquals(ErrorType.NEIGHBOR_FACT, ErrorClassifier.classify(key, given = 8, correct = 12))
    }

    @Test
    fun offByOne() {
        val key: FactKey = FactKey(FactKey.Op.ADD, 20, 30)
        assertEquals(ErrorType.OFF_BY_ONE, ErrorClassifier.classify(key, given = 51, correct = 50))
    }

    @Test
    fun offByTen() {
        val key: FactKey = FactKey(FactKey.Op.ADD, 2, 3)
        assertEquals(ErrorType.OFF_BY_TEN, ErrorClassifier.classify(key, given = 15, correct = 5))
    }

    @Test
    fun signErrorReversedSubtraction() {
        val key: FactKey = FactKey(FactKey.Op.SUB, 12, 5)
        assertEquals(ErrorType.SIGN_ERROR, ErrorClassifier.classify(key, given = -7, correct = 7))
    }

    @Test
    fun randomWhenNoPatternMatches() {
        val key: FactKey = FactKey(FactKey.Op.MUL, 7, 8)
        assertEquals(ErrorType.RANDOM, ErrorClassifier.classify(key, given = 99, correct = 56))
    }
}
