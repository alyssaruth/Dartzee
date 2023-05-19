package dartzee.dartzee.dart

import dartzee.bullseye
import dartzee.dartzee.AbstractDartzeeRuleTest
import dartzee.doubleTwenty
import dartzee.innerSingle
import dartzee.missTwenty
import dartzee.outerBull
import dartzee.outerSingle
import dartzee.trebleNineteen
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

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
        rule.isValidSegment(missTwenty) shouldBe false
    }
}