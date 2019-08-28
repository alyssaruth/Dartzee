package burlton.dartzee.test.dartzee.total

import burlton.dartzee.code.dartzee.total.DartzeeTotalRuleEqualTo
import burlton.dartzee.test.dartzee.AbstractDartzeeRuleTest
import io.kotlintest.shouldBe
import org.junit.Test

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
    fun `Partial total validation`()
    {
        val rule = DartzeeTotalRuleEqualTo()
        rule.target = 65

        rule.isPotentiallyValidTotal(63, 2) shouldBe true
        rule.isPotentiallyValidTotal(64, 1) shouldBe true
        rule.isPotentiallyValidTotal(5, 1) shouldBe true

        rule.isPotentiallyValidTotal(65, 1) shouldBe false
        rule.isPotentiallyValidTotal(64, 2) shouldBe false
        rule.isPotentiallyValidTotal(4, 1) shouldBe false
        rule.isPotentiallyValidTotal(70, 1) shouldBe false
    }
}