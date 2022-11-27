package dartzee.dartzee.dart

import dartzee.*
import dartzee.dartzee.AbstractDartzeeRuleTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

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