package dartzee.dartzee.aggregate

import dartzee.dartzee.AbstractDartzeeRuleTest
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestDartzeeTotalRuleMultipleOf: AbstractDartzeeRuleTest<DartzeeTotalRuleMultipleOf>()
{
    override fun factory() = DartzeeTotalRuleMultipleOf()

    @Test
    fun `Total validation`()
    {
        val rule = factory()
        rule.target = 7

        rule.isValidTotal(20) shouldBe false
        rule.isValidTotal(21) shouldBe true
        rule.isValidTotal(22) shouldBe false
    }

    @Test
    fun `Rule description`()
    {
        val rule = factory()
        rule.target = 9

        rule.getDescription() shouldBe "Total multiple of 9"
    }
}