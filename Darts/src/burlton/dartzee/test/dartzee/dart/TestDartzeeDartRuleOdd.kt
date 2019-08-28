package burlton.dartzee.test.dartzee.dart

import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleOdd
import burlton.dartzee.test.*
import burlton.dartzee.test.dartzee.AbstractDartzeeRuleTest
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestDartzeeDartRuleOdd: AbstractDartzeeRuleTest<DartzeeDartRuleOdd>()
{
    override fun factory() = DartzeeDartRuleOdd()

    @Test
    fun `segment validation for odd rule`()
    {
        val rule = DartzeeDartRuleOdd()

        assertTrue(rule.isValidSegment(doubleNineteen))
        assertFalse(rule.isValidSegment(trebleTwenty))
        assertTrue(rule.isValidSegment(singleNineteen))
        assertFalse(rule.isValidSegment(miss))
        assertFalse(rule.isValidSegment(missedBoard))
        assertTrue(rule.isValidSegment(outerBull))
    }
}