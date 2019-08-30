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
}