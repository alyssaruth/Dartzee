package burlton.dartzee.test.dartzee.dart

import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleOuter
import burlton.dartzee.test.*
import burlton.dartzee.test.dartzee.AbstractDartzeeRuleTest
import io.kotlintest.shouldBe
import org.junit.Test

class TestDartzeeDartRuleOuter: AbstractDartzeeRuleTest<DartzeeDartRuleOuter>()
{
    override fun factory() = DartzeeDartRuleOuter()

    @Test
    fun `segment validation`()
    {
        val rule = DartzeeDartRuleOuter()

        rule.isValidSegment(bullseye) shouldBe false
        rule.isValidSegment(outerBull) shouldBe false
        rule.isValidSegment(innerSingle) shouldBe false
        rule.isValidSegment(trebleNineteen) shouldBe false
        rule.isValidSegment(outerSingle) shouldBe true
        rule.isValidSegment(doubleTwenty) shouldBe true
        rule.isValidSegment(missTwenty) shouldBe false
        rule.isValidSegment(missedBoard) shouldBe false
    }
}