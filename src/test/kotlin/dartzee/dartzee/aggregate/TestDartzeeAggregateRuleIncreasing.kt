package dartzee.dartzee.aggregate

import dartzee.dartzee.AbstractDartzeeRuleTest
import dartzee.helper.double
import dartzee.helper.outerSingle
import dartzee.helper.treble
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestDartzeeAggregateRuleIncreasing :
    AbstractDartzeeRuleTest<DartzeeAggregateRuleIncreasing>() {
    override fun factory() = DartzeeAggregateRuleIncreasing()

    @Test
    fun `Should be valid if scores are increasing`() {
        val rule = factory()
        rule.isValidRound(listOf(outerSingle(5), outerSingle(10), outerSingle(15))) shouldBe true
        rule.isValidRound(listOf(outerSingle(10), double(6), treble(5))) shouldBe true
    }

    @Test
    fun `Should not be valid if two the same`() {
        val rule = factory()
        rule.isValidRound(listOf(outerSingle(5), outerSingle(10), outerSingle(10))) shouldBe false
        rule.isValidRound(listOf(outerSingle(12), double(6), treble(6))) shouldBe false
    }

    @Test
    fun `Should not be valid if not increasing`() {
        val rule = factory()
        rule.isValidRound(listOf(outerSingle(5), outerSingle(15), outerSingle(10))) shouldBe false
    }
}
