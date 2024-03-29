package dartzee.dartzee.aggregate

import dartzee.dartzee.AbstractDartzeeRuleTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestDartzeeTotalRuleGreaterThan : AbstractDartzeeRuleTest<DartzeeTotalRuleGreaterThan>() {
    override fun factory() = DartzeeTotalRuleGreaterThan()

    @Test
    fun `Total validation`() {
        val rule = DartzeeTotalRuleGreaterThan()
        rule.target = 55

        rule.isValidTotal(54) shouldBe false
        rule.isValidTotal(55) shouldBe false
        rule.isValidTotal(56) shouldBe true
    }

    @Test
    fun `Rule description`() {
        val rule = DartzeeTotalRuleGreaterThan()
        rule.target = 25

        rule.getDescription() shouldBe "Total > 25"
    }
}
