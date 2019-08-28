package burlton.dartzee.test.dartzee.total

import burlton.dartzee.code.dartzee.total.DartzeeTotalRuleLessThan
import burlton.dartzee.test.dartzee.AbstractDartzeeRuleTest
import io.kotlintest.shouldBe
import org.junit.Test

class TestDartzeeTotalRuleLessThan: AbstractDartzeeRuleTest<DartzeeTotalRuleLessThan>()
{
    override fun factory() = DartzeeTotalRuleLessThan()

    @Test
    fun `Total validation`()
    {
        val rule = DartzeeTotalRuleLessThan()
        rule.target = 55

        rule.isValidTotal(54) shouldBe true
        rule.isValidTotal(55) shouldBe false
        rule.isValidTotal(56) shouldBe false
    }

    @Test
    fun `Partial total validation`()
    {
        val rule = DartzeeTotalRuleLessThan()
        rule.target = 65

        rule.isPotentiallyValidTotal(63, 2) shouldBe false
        rule.isPotentiallyValidTotal(64, 1) shouldBe false
        rule.isPotentiallyValidTotal(63, 1) shouldBe true
        rule.isPotentiallyValidTotal(62, 2) shouldBe true

        rule.isPotentiallyValidTotal(2, 2) shouldBe true
        rule.isPotentiallyValidTotal(2, 1) shouldBe true
    }
}