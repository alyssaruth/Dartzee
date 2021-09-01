package dartzee.dartzee.aggregate

import dartzee.dartzee.AbstractDartzeeRuleTest
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestDartzeeTotalRuleEqualTo: AbstractDartzeeRuleTest<DartzeeTotalRuleEqualTo>()
{
    override fun factory() = DartzeeTotalRuleEqualTo()

    @Test
    fun `Total validation`()
    {
        val rule = DartzeeTotalRuleEqualTo()
        rule.target = 55

        rule.isValidTotal(54) shouldBe false
        rule.isValidTotal(55) shouldBe true
        rule.isValidTotal(56) shouldBe false
    }

    @Test
    fun `Rule description`()
    {
        val rule = DartzeeTotalRuleEqualTo()
        rule.target = 25

        rule.getDescription() shouldBe "Total = 25"
    }
}