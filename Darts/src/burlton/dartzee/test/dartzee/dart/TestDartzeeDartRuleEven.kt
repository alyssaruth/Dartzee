package burlton.dartzee.test.dartzee.dart

import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleEven
import burlton.dartzee.test.*
import burlton.dartzee.test.dartzee.AbstractDartzeeRuleTest
import io.kotlintest.shouldBe
import org.junit.Test

class TestDartzeeDartRuleEven: AbstractDartzeeRuleTest<DartzeeDartRuleEven>()
{
    override fun factory() = DartzeeDartRuleEven()

    @Test
    fun `segment validation`()
    {
        val rule = DartzeeDartRuleEven()

        rule.isValidSegment(doubleNineteen) shouldBe false
        rule.isValidSegment(trebleTwenty) shouldBe true
        rule.isValidSegment(outerBull) shouldBe false
        rule.isValidSegment(singleNineteen) shouldBe false
        rule.isValidSegment(missTwenty) shouldBe false
        rule.isValidSegment(missedBoard) shouldBe false
    }
}