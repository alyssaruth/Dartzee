package burlton.dartzee.test.dartzee.dart

import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleAny
import burlton.dartzee.test.*
import burlton.dartzee.test.dartzee.AbstractDartzeeRuleTest
import io.kotlintest.shouldBe
import org.junit.Test

class TestDartzeeDartRuleAny: AbstractDartzeeRuleTest<DartzeeDartRuleAny>()
{
    override fun factory() = DartzeeDartRuleAny()

    @Test
    fun `segment validation`()
    {
        val rule = DartzeeDartRuleAny()

        rule.isValidSegment(doubleNineteen) shouldBe true
        rule.isValidSegment(trebleTwenty) shouldBe true
        rule.isValidSegment(outerBull) shouldBe true
        rule.isValidSegment(singleNineteen) shouldBe true
        rule.isValidSegment(missTwenty) shouldBe true
        rule.isValidSegment(missedBoard) shouldBe true
    }
}