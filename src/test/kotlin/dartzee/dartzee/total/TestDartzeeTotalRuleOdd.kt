package dartzee.dartzee.total

import dartzee.dartzee.AbstractDartzeeRuleTest
import dartzee.dartzee.aggregate.DartzeeTotalRuleOdd
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestDartzeeTotalRuleOdd: AbstractDartzeeRuleTest<DartzeeTotalRuleOdd>()
{
    override fun factory() = DartzeeTotalRuleOdd()

    @Test
    fun `Total validation`()
    {
        val rule = DartzeeTotalRuleOdd()

        rule.isValidTotal(20) shouldBe false
        rule.isValidTotal(21) shouldBe true
    }

    @Test
    fun `Rule description`()
    {
        val rule = DartzeeTotalRuleOdd()
        rule.getDescription() shouldBe "is odd"
    }
}