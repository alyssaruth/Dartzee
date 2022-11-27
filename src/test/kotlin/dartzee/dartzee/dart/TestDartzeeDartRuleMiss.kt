package dartzee.dartzee.dart

import dartzee.*
import dartzee.dartzee.AbstractDartzeeRuleTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestDartzeeDartRuleMiss: AbstractDartzeeRuleTest<DartzeeDartRuleMiss>()
{
    override fun factory() = DartzeeDartRuleMiss()

    @Test
    fun `segment validation`()
    {
        val rule = factory()

        rule.isValidSegment(bullseye) shouldBe false
        rule.isValidSegment(outerBull) shouldBe false
        rule.isValidSegment(innerSingle) shouldBe false
        rule.isValidSegment(trebleNineteen) shouldBe false
        rule.isValidSegment(outerSingle) shouldBe false
        rule.isValidSegment(doubleTwenty) shouldBe false
        rule.isValidSegment(missTwenty) shouldBe true
        rule.isValidSegment(missedBoard) shouldBe true
    }
}