package dartzee.dartzee.aggregate

import dartzee.dartzee.AbstractDartzeeRuleTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestDartzeeTotalRuleLessThan : AbstractDartzeeRuleTest<DartzeeTotalRuleLessThan>() {
    override fun factory() = DartzeeTotalRuleLessThan()

    @Test
    fun `Total validation`() {
        val rule = DartzeeTotalRuleLessThan()
        rule.target = 55

        rule.isValidTotal(54) shouldBe true
        rule.isValidTotal(55) shouldBe false
        rule.isValidTotal(56) shouldBe false
    }

    @Test
    fun `Rule description`() {
        val rule = DartzeeTotalRuleLessThan()
        rule.target = 25

        rule.getDescription() shouldBe "Total < 25"
    }
}
