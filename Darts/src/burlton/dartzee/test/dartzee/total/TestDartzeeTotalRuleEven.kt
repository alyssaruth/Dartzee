package burlton.dartzee.test.dartzee.total

import burlton.dartzee.code.dartzee.total.DartzeeTotalRuleEven
import burlton.dartzee.test.dartzee.AbstractDartzeeRuleTest
import io.kotlintest.shouldBe
import org.junit.Test

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
        rule.getDescription() shouldBe "is even"
    }
}