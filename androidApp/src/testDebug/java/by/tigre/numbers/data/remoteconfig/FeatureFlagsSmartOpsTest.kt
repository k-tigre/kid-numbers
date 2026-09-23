package by.tigre.numbers.data.remoteconfig

import by.tigre.numbers.data.facts.parseOpsCsv
import by.tigre.numbers.entity.FactKey
import org.junit.Assert.assertEquals
import org.junit.Test

class FeatureFlagsSmartOpsTest {

    @Test
    fun parseOpsCsvParsesValidTokens() {
        assertEquals(
            setOf(FactKey.Op.MUL, FactKey.Op.ADD),
            parseOpsCsv("MUL,ADD", fallback = FeatureFlagsImpl.DEFAULT_SMART_OPS_SET),
        )
    }

    @Test
    fun parseOpsCsvIgnoresInvalidTokens() {
        assertEquals(
            setOf(FactKey.Op.MUL, FactKey.Op.DIV),
            parseOpsCsv("MUL,FOO,DIV", fallback = FeatureFlagsImpl.DEFAULT_SMART_OPS_SET),
        )
    }

    @Test
    fun parseOpsCsvFallsBackWhenEmptyAfterParse() {
        assertEquals(
            FeatureFlagsImpl.DEFAULT_SMART_OPS_SET,
            parseOpsCsv("BAD,INVALID", fallback = FeatureFlagsImpl.DEFAULT_SMART_OPS_SET),
        )
    }

    @Test
    fun defaultSmartOpsIncludesAllFourOperations() {
        assertEquals(
            setOf(FactKey.Op.ADD, FactKey.Op.SUB, FactKey.Op.MUL, FactKey.Op.DIV),
            FeatureFlagsImpl.DEFAULT_SMART_OPS_SET,
        )
        assertEquals("ADD,SUB,MUL,DIV", FeatureFlagsImpl.DEFAULT_SMART_OPS)
    }

    @Test
    fun defaultSmartPracticeEnabledIsOn() {
        assertEquals(true, FeatureFlagsImpl.DEFAULT_SMART_PRACTICE_ENABLED)
        assertEquals(
            FeatureFlagsImpl.DEFAULT_SMART_PRACTICE_ENABLED,
            LocalRemoteConfigDefaults.defaults[RemoteConfigKeys.SMART_PRACTICE_ENABLED],
        )
    }
}
