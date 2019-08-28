package burlton.dartzee.test.dartzee.total

import burlton.dartzee.code.dartzee.total.DartzeeTotalRuleOdd
import burlton.dartzee.test.dartzee.AbstractDartzeeRuleTest
import io.kotlintest.shouldBe
import org.junit.Test

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
    fun `Partial total validation`()
    {
        val rule = DartzeeTotalRuleOdd()

        rule.isPotentiallyValidTotal(20, 2) shouldBe true
        rule.isPotentiallyValidTotal(21, 2) shouldBe true
    }
}