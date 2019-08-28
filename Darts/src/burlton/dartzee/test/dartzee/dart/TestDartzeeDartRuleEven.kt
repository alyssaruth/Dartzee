package burlton.dartzee.test.dartzee.dart

import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleEven
import burlton.dartzee.test.*
import burlton.dartzee.test.dartzee.AbstractDartzeeRuleTest
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestDartzeeDartRuleEven: AbstractDartzeeRuleTest<DartzeeDartRuleEven>()
{
    override fun factory() = DartzeeDartRuleEven()

    @Test
    fun `segment validation`()
    {
        val rule = DartzeeDartRuleEven()

        assertFalse(rule.isValidSegment(doubleNineteen))
        assertTrue(rule.isValidSegment(trebleTwenty))
        assertFalse(rule.isValidSegment(outerBull))
        assertFalse(rule.isValidSegment(singleNineteen))
        assertFalse(rule.isValidSegment(miss))
        assertFalse(rule.isValidSegment(missedBoard))
    }
}