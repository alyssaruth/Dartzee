package burlton.dartzee.test.dartzee.dart

import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleOdd
import burlton.dartzee.test.*
import burlton.dartzee.test.dartzee.AbstractDartzeeRuleTest
import io.kotlintest.shouldBe
import org.junit.Test

class TestDartzeeDartRuleOdd: AbstractDartzeeRuleTest<DartzeeDartRuleOdd>()
{
    override fun factory() = DartzeeDartRuleOdd()

    @Test
    fun `segment validation for odd rule`()
    {
        val rule = DartzeeDartRuleOdd()

        rule.isValidSegment(doubleNineteen) shouldBe true
        rule.isValidSegment(trebleTwenty) shouldBe false
        rule.isValidSegment(singleNineteen) shouldBe true
        rule.isValidSegment(miss) shouldBe false
        rule.isValidSegment(missedBoard) shouldBe false
        rule.isValidSegment(outerBull) shouldBe true
    }
}