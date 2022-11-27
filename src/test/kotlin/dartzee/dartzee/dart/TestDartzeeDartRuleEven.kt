package dartzee.dartzee.dart

import dartzee.*
import dartzee.dartzee.AbstractDartzeeRuleTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

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