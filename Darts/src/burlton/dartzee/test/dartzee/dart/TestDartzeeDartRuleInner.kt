package burlton.dartzee.test.dartzee.dart

import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleInner
import burlton.dartzee.test.*
import burlton.dartzee.test.dartzee.AbstractDartzeeRuleTest
import io.kotlintest.shouldBe
import org.junit.Test

class TestDartzeeDartRuleInner: AbstractDartzeeRuleTest<DartzeeDartRuleInner>()
{
    override fun factory() = DartzeeDartRuleInner()

    @Test
    fun `segment validation`()
    {
        val rule = DartzeeDartRuleInner()

        rule.isValidSegment(bullseye) shouldBe true
        rule.isValidSegment(outerBull) shouldBe true
        rule.isValidSegment(innerSingle) shouldBe true
        rule.isValidSegment(trebleNineteen) shouldBe true
        rule.isValidSegment(outerSingle) shouldBe false
        rule.isValidSegment(doubleTwenty) shouldBe false
        rule.isValidSegment(miss) shouldBe false
        rule.isValidSegment(missedBoard) shouldBe false
    }
}