package dartzee.dartzee.aggregate

import dartzee.dartzee.AbstractDartzeeRuleTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestDartzeeTotalRulePrime : AbstractDartzeeRuleTest<DartzeeTotalRulePrime>() {
    override fun factory() = DartzeeTotalRulePrime()

    @Test
    fun `Total validation`() {
        val rule = DartzeeTotalRulePrime()

        rule.isValidTotal(2) shouldBe true
        rule.isValidTotal(3) shouldBe true
        rule.isValidTotal(7) shouldBe true
        rule.isValidTotal(23) shouldBe true

        rule.isValidTotal(6) shouldBe false
        rule.isValidTotal(21) shouldBe false
    }
}
