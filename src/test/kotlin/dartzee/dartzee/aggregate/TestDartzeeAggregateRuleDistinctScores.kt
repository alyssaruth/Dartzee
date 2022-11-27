package dartzee.dartzee.aggregate

import dartzee.dartzee.AbstractDartzeeRuleTest
import dartzee.helper.double
import dartzee.helper.miss
import dartzee.helper.outerSingle
import dartzee.helper.treble
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestDartzeeAggregateRuleDistinctScores: AbstractDartzeeRuleTest<DartzeeAggregateRuleDistinctScores>()
{
    override fun factory() = DartzeeAggregateRuleDistinctScores()

    @Test
    fun `Should not allow misses`()
    {
        factory().isValidRound(listOf(miss(20), outerSingle(11), double(3))) shouldBe false
    }

    @Test
    fun `Should be valid if all scores are distinct`()
    {
        factory().isValidRound(listOf(outerSingle(20), treble(11), double(3))) shouldBe true
    }

    @Test
    fun `Should not be valid if one or more scores are repeated`()
    {
        factory().isValidRound(listOf(outerSingle(20), treble(20), double(3))) shouldBe false
        factory().isValidRound(listOf(outerSingle(20), double(20), treble(20))) shouldBe false
        factory().isValidRound(listOf(outerSingle(5), outerSingle(10), double(5))) shouldBe false
    }
}