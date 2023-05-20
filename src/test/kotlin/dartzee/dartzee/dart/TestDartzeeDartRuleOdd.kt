package dartzee.dartzee.dart

import dartzee.dartzee.AbstractDartzeeRuleTest
import dartzee.doubleNineteen
import dartzee.missTwenty
import dartzee.outerBull
import dartzee.singleNineteen
import dartzee.trebleTwenty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestDartzeeDartRuleOdd: AbstractDartzeeRuleTest<DartzeeDartRuleOdd>()
{
    override fun factory() = DartzeeDartRuleOdd()

    @Test
    fun `segment validation`()
    {
        val rule = DartzeeDartRuleOdd()

        rule.isValidSegment(doubleNineteen) shouldBe true
        rule.isValidSegment(trebleTwenty) shouldBe false
        rule.isValidSegment(singleNineteen) shouldBe true
        rule.isValidSegment(missTwenty) shouldBe false
        rule.isValidSegment(outerBull) shouldBe true
    }
}