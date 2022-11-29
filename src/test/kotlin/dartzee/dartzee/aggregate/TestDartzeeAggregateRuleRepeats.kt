package dartzee.dartzee.aggregate

import dartzee.`object`.Dart
import dartzee.dartzee.AbstractDartzeeRuleTest
import dartzee.helper.double
import dartzee.helper.miss
import dartzee.helper.outerSingle
import dartzee.helper.treble
import dartzee.utils.sumScore
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestDartzeeAggregateRuleRepeats: AbstractDartzeeRuleTest<DartzeeAggregateRuleRepeats>()
{
    override fun factory() = DartzeeAggregateRuleRepeats()

    @Test
    fun `Should not count a missed dart as a repeat`()
    {
        factory().isValidRound(listOf(miss(20), outerSingle(20), double(3))) shouldBe false
    }

    @Test
    fun `Should be valid if at least one score is repeated`()
    {
        factory().isValidRound(listOf(outerSingle(20), treble(20), miss(3))) shouldBe true
        factory().isValidRound(listOf(outerSingle(5), double(20), treble(5))) shouldBe true
        factory().isValidRound(listOf(outerSingle(20), double(20), treble(20))) shouldBe true
    }

    @Test
    fun `Should not be valid if all scores are distinct`()
    {
        factory().isValidRound(listOf(outerSingle(20), treble(5), double(3))) shouldBe false
        factory().isValidRound(listOf(outerSingle(20), double(25), treble(14))) shouldBe false
    }

    @Test
    fun `Should only score the darts that were repeats`()
    {
        factory().getScore(Dart(20, 1), Dart(20, 2), Dart(5, 1)) shouldBe 60
        factory().getScore(Dart(5, 1), Dart(5, 2), Dart(5, 3)) shouldBe 30
        factory().getScore(Dart(20, 1), Dart(20, 0), Dart(20, 1)) shouldBe 40
        factory().getScore(Dart(5, 1), Dart(20, 0), Dart(5, 1)) shouldBe 10
    }

    private fun DartzeeAggregateRuleRepeats.getScore(vararg darts: Dart): Int
    {
        return sumScore(getScoringDarts(darts.toList()))
    }
}