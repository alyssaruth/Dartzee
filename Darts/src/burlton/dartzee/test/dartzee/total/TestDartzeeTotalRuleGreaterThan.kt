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
    fun `Rule description`()
    {
        val rule = DartzeeTotalRuleGreaterThan()
        rule.target = 25

        rule.getDescription() shouldBe "> 25"
    }
}