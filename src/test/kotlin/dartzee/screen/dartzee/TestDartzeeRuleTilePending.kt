package dartzee.screen.dartzee

import dartzee.dartzee.DartzeeCalculator
import dartzee.doubleNineteen
import dartzee.helper.AbstractTest
import dartzee.helper.makeDart
import dartzee.helper.makeDartzeeRuleCalculationResult
import dartzee.helper.makeDartzeeRuleDto
import dartzee.helper.makeScoreRule
import dartzee.`object`.SegmentType
import dartzee.screen.game.dartzee.DartzeeRuleTilePending
import dartzee.trebleTwenty
import dartzee.utils.InjectedThings
import dartzee.utils.getAllNonMissSegments
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import java.awt.Color
import org.junit.jupiter.api.Test

class TestDartzeeRuleTilePending : AbstractTest() {
    @Test
    fun `Should initially have no pending result or score`() {
        val tile = DartzeeRuleTilePending(makeDartzeeRuleDto(), 5)

        tile.pendingScore shouldBe null
        tile.pendingResult shouldBe null
        tile.getScoreForHover() shouldBe null
    }

    @Test
    fun `Should show or hide based on whether the rule is valid for the darts thrown`() {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val tile =
            DartzeeRuleTilePending(makeDartzeeRuleDto(makeScoreRule(15), allowMisses = false), 5)
        tile.updateState(listOf(makeDart(20, 0, SegmentType.MISS)))
        tile.isVisible shouldBe false

        tile.updateState(listOf(makeDart(20, 1, SegmentType.OUTER_SINGLE)))
        tile.isVisible shouldBe true
    }

    @Test
    fun `Should use the cached calculation result when getting valid segments and no darts have been thrown`() {
        val segments = listOf(doubleNineteen, trebleTwenty)
        val rule =
            makeDartzeeRuleDto(
                calculationResult = makeDartzeeRuleCalculationResult(validSegments = segments)
            )

        val tile = DartzeeRuleTilePending(rule, 2)
        tile
            .getSegmentStatus(listOf())
            .validSegments
            .shouldContainExactlyInAnyOrder(doubleNineteen, trebleTwenty)
    }

    @Test
    fun `Should calculate valid segments based on the rule if one or more darts have been thrown`() {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val rule =
            makeDartzeeRuleDto(
                makeScoreRule(20),
                makeScoreRule(18),
                makeScoreRule(19),
                inOrder = true,
            )
        val tile = DartzeeRuleTilePending(rule, 2)

        tile
            .getSegmentStatus(listOf(makeDart(18, 1, SegmentType.OUTER_SINGLE)))
            .validSegments
            .shouldBeEmpty()

        val eighteenSegments = getAllNonMissSegments().filter { it.score == 18 }.toTypedArray()
        tile
            .getSegmentStatus(listOf(makeDart(20, 1, SegmentType.OUTER_SINGLE)))
            .validSegments
            .shouldContainExactlyInAnyOrder(*eighteenSegments)

        val twoDarts =
            listOf(makeDart(20, 2, SegmentType.DOUBLE), makeDart(18, 2, SegmentType.DOUBLE))
        val nineteenSegments = getAllNonMissSegments().filter { it.score == 19 }.toTypedArray()
        tile
            .getSegmentStatus(twoDarts)
            .validSegments
            .shouldContainExactlyInAnyOrder(*nineteenSegments)
    }

    @Test
    fun `Should set colour to red or green when a pending result is set`() {
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
