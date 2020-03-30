package dartzee.screen.game.golf

import dartzee.`object`.SEGMENT_TYPE_INNER_SINGLE
import dartzee.`object`.SEGMENT_TYPE_OUTER_SINGLE
import dartzee.game.state.DefaultPlayerState
import dartzee.helper.makeDart
import dartzee.helper.makeDefaultPlayerState
import dartzee.helper.makeDefaultPlayerStateWithRounds
import dartzee.helper.makeGolfRound
import dartzee.screen.game.AbstractGameStatisticsPanelTest
import dartzee.screen.game.getValueForRow
import dartzee.screen.game.scorer.DartsScorerGolf
import io.kotlintest.shouldBe
import org.junit.Test

class TestGameStatisticsPanelGolf: AbstractGameStatisticsPanelTest<DefaultPlayerState<DartsScorerGolf>, GameStatisticsPanelGolf>()
{
    override fun factoryStatsPanel() = GameStatisticsPanelGolf()

    override fun makePlayerState(): DefaultPlayerState<DartsScorerGolf>
    {
        val roundOne = makeGolfRound(1, listOf(makeDart(1, 1), makeDart(1, 1)))
        val roundTwo = makeGolfRound(2, listOf(makeDart(17, 1), makeDart(2, 3)))

        return makeDefaultPlayerStateWithRounds(dartsThrown = listOf(roundOne, roundTwo))
    }

    @Test
    fun `Should get best, avg and worst hole state correct`()
    {
        val roundOne = makeGolfRound(1, listOf(makeDart(1, 1, segmentType = SEGMENT_TYPE_OUTER_SINGLE)))

        val state = makeDefaultPlayerState<DartsScorerGolf>(dartsThrown = roundOne)

        //4
        val statsPanel = factoryStatsPanel()
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Best Hole") shouldBe 4
        statsPanel.getValueForRow("Worst Hole") shouldBe 4
        statsPanel.getValueForRow("Avg. Hole") shouldBe 4.0

        //4, 2
        val roundTwo = makeGolfRound(2, listOf(makeDart(5, 1), makeDart(2, 3)))
        state.addDarts(roundTwo)
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Best Hole") shouldBe 2
        statsPanel.getValueForRow("Worst Hole") shouldBe 4
        statsPanel.getValueForRow("Avg. Hole") shouldBe 3.0

        //4, 2, 5
        val roundThree = makeGolfRound(3, listOf(makeDart(3, 1), makeDart(2, 1), makeDart(3, 0)))
        state.addDarts(roundThree)
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Best Hole") shouldBe 2
        statsPanel.getValueForRow("Worst Hole") shouldBe 5
        statsPanel.getValueForRow("Avg. Hole") shouldBe 3.67

        //4, 2, 5, 1
        val roundFour = makeGolfRound(4, listOf(makeDart(4, 2)))
        state.addDarts(roundFour)
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Best Hole") shouldBe 1
        statsPanel.getValueForRow("Worst Hole") shouldBe 5
        statsPanel.getValueForRow("Avg. Hole") shouldBe 3.0
    }

    @Test
    fun `Should correctly calculate miss %`()
    {
        val roundOne = makeGolfRound(1, listOf(makeDart(1, 1, segmentType = SEGMENT_TYPE_OUTER_SINGLE)))

        val state = makeDefaultPlayerState<DartsScorerGolf>(dartsThrown = roundOne)

        // [Hit]
        val statsPanel = factoryStatsPanel()
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Miss %") shouldBe 0.0

        // [Hit, Miss, Hit]
        val roundTwo = makeGolfRound(2, listOf(makeDart(5, 1), makeDart(2, 3)))
        state.addDarts(roundTwo)
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Miss %") shouldBe 33.3

        // [Hit, Miss, Hit, Hit Miss, Miss]
        val roundThree = makeGolfRound(3, listOf(makeDart(3, 1), makeDart(2, 1), makeDart(3, 0)))
        state.addDarts(roundThree)
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Miss %") shouldBe 50.0
    }

    @Test
    fun `Should correctly populate points squandered`()
    {
        // 3-5-4
        val roundOne = makeGolfRound(1, listOf(makeDart(1, 1, segmentType = SEGMENT_TYPE_INNER_SINGLE), makeDart(20, 1), makeDart(1, 1)))
        val state = makeDefaultPlayerState<DartsScorerGolf>(dartsThrown = roundOne)

        val statsPanel = factoryStatsPanel()
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Points Squandered") shouldBe 1

        // 4-5-4, no change
        val roundTwo = makeGolfRound(2, listOf(makeDart(2, 1), makeDart(20, 1), makeDart(2, 1)))
        state.addDarts(roundTwo)
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Points Squandered") shouldBe 1

        // 2-5-5, another 3 squandered
        val roundThree = makeGolfRound(3, listOf(makeDart(3, 3), makeDart(3, 0), makeDart(19, 2)))
        state.addDarts(roundThree)
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Points Squandered") shouldBe 4
    }

    /**
     * 4-2-3. You've gained 1 (and also lost 1). Method should return 1 for the original '4' gamble. I guess.
     */
    @Test
    fun `Should correctly populate points improved`()
    {
        //4-3-2. You've gambled twice, and gained 1 each time. 2 points improved.
        val roundOne = makeGolfRound(1, listOf(makeDart(1, 1), makeDart(1, 1, SEGMENT_TYPE_INNER_SINGLE), makeDart(1, 3)))
        val state = makeDefaultPlayerState<DartsScorerGolf>(dartsThrown = roundOne)
        val statsPanel = factoryStatsPanel()
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Points Improved") shouldBe 2

        //5-4-3. You didn't gamble the first one, so have only gained 1.
        val roundTwo = makeGolfRound(2, listOf(makeDart(2, 0), makeDart(2, 1), makeDart(2, 1, SEGMENT_TYPE_INNER_SINGLE)))
        state.addDarts(roundTwo)
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Points Improved") shouldBe 3

        //3-5-2. You've gambled the 3, stuffed it, then clawed it back. Improved by 1
        val roundThree = makeGolfRound(3, listOf(makeDart(3, 1, SEGMENT_TYPE_INNER_SINGLE), makeDart(3, 0), makeDart(3, 3)))
        state.addDarts(roundThree)
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Points Improved") shouldBe 4

        //5-5-1. You've not gambled anything. Method should return 0.
        val roundFour = makeGolfRound(4, listOf(makeDart(4, 0), makeDart(4, 0), makeDart(4, 2)))
        state.addDarts(roundFour)
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Points Improved") shouldBe 4

        //4-2-5. You've stuffed it - there was a gain but it's gone. Method should return 0.
        val roundFive = makeGolfRound(5, listOf(makeDart(5, 1), makeDart(5, 3), makeDart(5, 0)))
        state.addDarts(roundFive)
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Points Improved") shouldBe 4

        //4-2-3. You've gained 1 (and also lost 1). 0.
        val roundSix = makeGolfRound(6, listOf(makeDart(6, 1), makeDart(6, 3), makeDart(6, 1, SEGMENT_TYPE_INNER_SINGLE)))
        state.addDarts(roundSix)
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Points Improved") shouldBe 4
    }

    @Test
    fun `Should calculate score breakdown correctly`()
    {
        val roundOne = makeGolfRound(1, listOf(makeDart(1, 1, segmentType = SEGMENT_TYPE_OUTER_SINGLE)))

        val state = makeDefaultPlayerState<DartsScorerGolf>(dartsThrown = roundOne)

        //4
        val statsPanel = factoryStatsPanel()
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(mapOf("4" to 1))

        //4, 2
        val roundTwo = makeGolfRound(2, listOf(makeDart(2, 3)))
        state.addDarts(roundTwo)
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(mapOf("4" to 1, "2" to 1))

        //4, 2, 5
        val roundThree = makeGolfRound(3, listOf(makeDart(3, 0)))
        state.addDarts(roundThree)
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(mapOf("4" to 1, "2" to 1, "5" to 1))

        //4, 2, 5, 5
        val roundFour = makeGolfRound(4, listOf(makeDart(4, 0)))
        state.addDarts(roundFour)
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(mapOf("4" to 1, "2" to 1, "5" to 2))

        //4, 2, 5, 5, 3
        val roundFive = makeGolfRound(5, listOf(makeDart(5, 1, SEGMENT_TYPE_INNER_SINGLE)))
        state.addDarts(roundFive)
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(mapOf("4" to 1, "2" to 1, "5" to 2, "3" to 1))

        //4, 2, 5, 5, 3, 1
        val roundSix = makeGolfRound(6, listOf(makeDart(6, 2)))
        state.addDarts(roundSix)
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(mapOf("4" to 1, "2" to 1, "5" to 2, "3" to 1, "1" to 1))
    }

}