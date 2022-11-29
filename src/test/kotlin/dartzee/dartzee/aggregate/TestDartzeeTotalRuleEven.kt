package dartzee.dartzee.aggregate

import dartzee.dartzee.AbstractDartzeeRuleTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestDartzeeTotalRuleEven: AbstractDartzeeRuleTest<DartzeeTotalRuleEven>()
{
    override fun factory() = DartzeeTotalRuleEven()

    @Test
    fun `Total validation`()
    {
        val rule = DartzeeTotalRuleEven()

        rule.isValidTotal(20) shouldBe true
        rule.isValidTotal(21) shouldBe false
    }

    @Test
    fun `Rule description`()
    {
        val rule = DartzeeTotalRuleEven()
        rule.getDescription() shouldBe "Total is even"
    }
}