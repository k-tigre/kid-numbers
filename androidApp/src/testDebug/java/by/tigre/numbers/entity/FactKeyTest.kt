package by.tigre.numbers.entity

import by.tigre.numbers.entity.GameOptions.Question.Operation.Additional
import by.tigre.numbers.entity.GameOptions.Question.Operation.Division
import by.tigre.numbers.entity.GameOptions.Question.Operation.Multiplication
import by.tigre.numbers.entity.GameOptions.Question.Operation.Subtraction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class FactKeyTest {

    @Test
    fun additionCanonicalizesOperands() {
        val fromThreeNine: FactKey = FactKey.fromQuestion(Additional(a = 3, b = 9))!!
        val fromNineThree: FactKey = FactKey.fromQuestion(Additional(a = 9, b = 3))!!
        assertEquals(FactKey(FactKey.Op.ADD, 3, 9), fromThreeNine)
        assertEquals(fromThreeNine, fromNineThree)
        assertEquals("ADD:3:9", fromThreeNine.toStorageKey())
    }

    @Test
    fun multiplicationCanonicalizesOperands() {
        val fromThreeSeven: FactKey = FactKey.fromQuestion(Multiplication(first = 3, second = 7))!!
        val fromSevenThree: FactKey = FactKey.fromQuestion(Multiplication(first = 7, second = 3))!!
        assertEquals(FactKey(FactKey.Op.MUL, 3, 7), fromThreeSeven)
        assertEquals(fromThreeSeven, fromSevenThree)
        assertEquals("MUL:3:7", fromThreeSeven.toStorageKey())
    }

    @Test
    fun subtractionPreservesOrder() {
        val twelveMinusFive: FactKey = FactKey.fromQuestion(Subtraction(b = 5, x = 7))!!
        val fiveMinusTwelve: FactKey = FactKey.fromQuestion(Subtraction(b = 12, x = -7))!!
        assertEquals(FactKey(FactKey.Op.SUB, 12, 5), twelveMinusFive)
        assertEquals(FactKey(FactKey.Op.SUB, 5, 12), fiveMinusTwelve)
        assertNotEquals(twelveMinusFive, fiveMinusTwelve)
        assertEquals("SUB:12:5", twelveMinusFive.toStorageKey())
    }

    @Test
    fun divisionUsesDividendAndDivisor() {
        val key: FactKey = FactKey.fromQuestion(Division(second = 7, x = 8))!!
        assertEquals(FactKey(FactKey.Op.DIV, 56, 7), key)
        assertEquals("DIV:56:7", key.toStorageKey())
    }

    @Test
    fun storageKeyRoundTrip() {
        val original: FactKey = FactKey(FactKey.Op.MUL, 7, 8)
        assertEquals(original, FactKey.parse(original.toStorageKey()))
    }

    @Test
    fun fromQuestionMapsAllOperationTypes() {
        assertEquals(FactKey(FactKey.Op.ADD, 2, 3), FactKey.fromQuestion(Additional(a = 2, b = 3)))
        assertEquals(FactKey(FactKey.Op.SUB, 10, 4), FactKey.fromQuestion(Subtraction(b = 4, x = 6)))
        assertEquals(FactKey(FactKey.Op.MUL, 4, 5), FactKey.fromQuestion(Multiplication(first = 4, second = 5)))
        assertEquals(FactKey(FactKey.Op.DIV, 20, 4), FactKey.fromQuestion(Division(second = 4, x = 5)))
    }
}
