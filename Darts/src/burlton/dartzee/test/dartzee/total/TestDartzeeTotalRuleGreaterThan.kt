package burlton.dartzee.test.dartzee.total

import burlton.dartzee.code.dartzee.total.DartzeeTotalRuleGreaterThan
import burlton.dartzee.test.dartzee.AbstractDartzeeRuleTest
import io.kotlintest.shouldBe
import org.junit.Test

class TestDartzeeTotalRuleGreaterThan: AbstractDartzeeRuleTest<DartzeeTotalRuleGreaterThan>()
{
    override fun factory() = DartzeeTotalRuleGreaterThan()

    @Test
    fun `Total validation`()
    {
        val rule = DartzeeTotalRuleGreaterThan()
        rule.target = 55

        rule.isValidTotal(54) shouldBe false
        rule.isValidTotal(55) shouldBe false
        rule.isValidTotal(56) shouldBe true
    }

    @Test
    fun `Partial total validation`()
    {
        val rule = DartzeeTotalRuleGreaterThan()
        rule.target = 65

        rule.isPotentiallyValidTotal(80, 2) shouldBe true
        rule.isPotentiallyValidTotal(6, 1) shouldBe true
        rule.isPotentiallyValidTotal(5, 1) shouldBe false

        rule.target = 179
        rule.isPotentiallyValidTotal(59, 2) shouldBe false
        rule.isPotentiallyValidTotal(119, 1) shouldBe false
        rule.isPotentiallyValidTotal(60, 2) shouldBe true
        rule.isPotentiallyValidTotal(120, 2) shouldBe true
    }
}