package dartzee.screen.dartzee

import dartzee.`object`.SEGMENT_TYPE_DOUBLE
import dartzee.`object`.SEGMENT_TYPE_MISS
import dartzee.`object`.SEGMENT_TYPE_OUTER_SINGLE
import dartzee.dartzee.DartzeeCalculator
import dartzee.doubleNineteen
import dartzee.helper.*
import dartzee.screen.game.dartzee.DartzeeRuleTilePending
import dartzee.trebleTwenty
import dartzee.utils.InjectedThings
import dartzee.utils.getAllPossibleSegments
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import org.junit.Test
import java.awt.Color

class TestDartzeeRuleTilePending: AbstractTest()
{
    override fun afterEachTest()
    {
        super.afterEachTest()
        InjectedThings.dartzeeCalculator = FakeDartzeeCalculator()
    }

    @Test
    fun `Should initially have no pending result or score`()
    {
        val tile = DartzeeRuleTilePending(makeDartzeeRuleDto(), 5)

        tile.pendingScore shouldBe null
        tile.pendingResult shouldBe null
        tile.getScoreForHover() shouldBe null
    }

    @Test
    fun `Should show or hide based on whether the rule is valid for the darts thrown`()
    {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val tile = DartzeeRuleTilePending(
            makeDartzeeRuleDto(allowMisses = false),
            5
        )
        tile.updateState(listOf(makeDart(20, 0, SEGMENT_TYPE_MISS)))
        tile.isVisible shouldBe false

        tile.updateState(listOf(makeDart(20, 1, SEGMENT_TYPE_OUTER_SINGLE)))
        tile.isVisible shouldBe true
    }

    @Test
    fun `Should use the cached calculation result when getting valid segments and no darts have been thrown`()
    {
        val segments = listOf(doubleNineteen, trebleTwenty)
        val rule = makeDartzeeRuleDto(calculationResult = makeDartzeeRuleCalculationResult(segments))

        val tile = DartzeeRuleTilePending(rule, 2)
        tile.getValidSegments(listOf()).shouldContainExactlyInAnyOrder(doubleNineteen, trebleTwenty)
    }

    @Test
    fun `Should calculate valid segments based on the rule if one or more darts have been thrown`()
    {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val rule = makeDartzeeRuleDto(makeScoreRule(20), makeScoreRule(18), makeScoreRule(19), inOrder = true)
        val tile = DartzeeRuleTilePending(rule, 2)

        tile.getValidSegments(listOf(makeDart(18, 1, SEGMENT_TYPE_OUTER_SINGLE))).shouldBeEmpty()

        val eighteenSegments = getAllPossibleSegments().filter { it.score == 18 && !it.isMiss()}.toTypedArray()
        tile.getValidSegments(listOf(makeDart(20, 1, SEGMENT_TYPE_OUTER_SINGLE))).shouldContainExactlyInAnyOrder(*eighteenSegments)

        val twoDarts = listOf(makeDart(20, 2, SEGMENT_TYPE_DOUBLE), makeDart(18, 2, SEGMENT_TYPE_DOUBLE))
        val nineteenSegments = getAllPossibleSegments().filter { it.score == 19 && !it.isMiss()}.toTypedArray()
        tile.getValidSegments(twoDarts).shouldContainExactlyInAnyOrder(*nineteenSegments)
    }

    @Test
    fun `Should set colour to red or green when a pending result is set`()
    {
        val tile = DartzeeRuleTilePending(makeDartzeeRuleDto(), 2)

        tile.setPendingResult(true, 50)
        tile.pendingResult shouldBe true
        tile.pendingScore shouldBe 50
        tile.getScoreForHover() shouldBe 50
        tile.background shouldBe Color.GREEN

        tile.setPendingResult(false, -25)
        tile.pendingResult shouldBe false
        tile.pendingScore shouldBe -25
        tile.getScoreForHover() shouldBe -25
        tile.background shouldBe Color.RED
    }
}