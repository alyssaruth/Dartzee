package dartzee.dartzee.aggregate

import dartzee.dartzee.AbstractDartzeeRuleTest
import dartzee.helper.double
import dartzee.helper.outerSingle
import dartzee.helper.treble
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestDartzeeAggregateRuleDecreasing: AbstractDartzeeRuleTest<DartzeeAggregateRuleDecreasing>()
{
    override fun factory() = DartzeeAggregateRuleDecreasing()

    @Test
    fun `Should be valid if scores are decreasing`()
    {
        val rule = factory()
        rule.isValidRound(listOf(outerSingle(15), outerSingle(10), outerSingle(5))) shouldBe true
        rule.isValidRound(listOf(treble(5), double(6), outerSingle(10))) shouldBe true
    }

    @Test
    fun `Should not be valid if two the same`()
    {
        val rule = factory()
        rule.isValidRound(listOf(outerSingle(10), outerSingle(10), outerSingle(5))) shouldBe false
        rule.isValidRound(listOf(outerSingle(12), double(6), outerSingle(6))) shouldBe false
    }

    @Test
    fun `Should not be valid if not decreasing`()
    {
        val rule = factory()
        rule.isValidRound(listOf(outerSingle(5), outerSingle(15), outerSingle(10))) shouldBe false
    }
}