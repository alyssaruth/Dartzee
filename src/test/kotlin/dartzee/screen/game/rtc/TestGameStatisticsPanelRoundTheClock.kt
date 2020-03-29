package dartzee.screen.game.rtc

import dartzee.db.CLOCK_TYPE_DOUBLES
import dartzee.db.CLOCK_TYPE_STANDARD
import dartzee.game.state.DefaultPlayerState
import dartzee.helper.makeDart
import dartzee.helper.makeDefaultPlayerStateWithRounds
import dartzee.screen.game.AbstractGameStatisticsPanelTest
import dartzee.screen.game.getValueForRow
import dartzee.screen.game.scorer.DartsScorerRoundTheClock
import io.kotlintest.shouldBe
import org.junit.Test

class TestGameStatisticsPanelRoundTheClock: AbstractGameStatisticsPanelTest<DefaultPlayerState<DartsScorerRoundTheClock>, GameStatisticsPanelRoundTheClock>()
{
    override fun factoryStatsPanel() = GameStatisticsPanelRoundTheClock(CLOCK_TYPE_STANDARD)

    override fun makePlayerState(): DefaultPlayerState<DartsScorerRoundTheClock>
    {
        val roundOne = listOf(makeDart(5, 1, startingScore = 1), makeDart(1, 1, startingScore = 1), makeDart(2, 3, startingScore = 2))
        val roundTwo = listOf(makeDart(3, 1, startingScore = 3), makeDart(4, 1, startingScore = 4), makeDart(5, 1, startingScore = 5), makeDart(6, 0, startingScore = 6))

        return makeDefaultPlayerStateWithRounds(dartsThrown = listOf(roundOne, roundTwo))
    }

    @Test
    fun `Most darts thrown should include unfinished rounds`()
    {
        val roundOne = listOf(makeDart(5, 1, startingScore = 1), makeDart(1, 1, startingScore = 1), makeDart(3, 1, startingScore = 2))

        val state = makeDefaultPlayerStateWithRounds<DartsScorerRoundTheClock>(dartsThrown = listOf(roundOne))
        val statsPanel = GameStatisticsPanelRoundTheClock(CLOCK_TYPE_STANDARD)

        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Most darts") shouldBe 2

        state.addDarts(listOf(makeDart(15, 1, startingScore = 2), makeDart(17, 1, startingScore = 2), makeDart(2, 0, startingScore = 2)))
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Most darts") shouldBe 4
    }

    @Test
    fun `Should calculate avg and fewest darts correctly, ignoring unfinished rounds`()
    {
        val roundOne = listOf(makeDart(5, 1, startingScore = 1), makeDart(1, 0, startingScore = 1), makeDart(20, 3, startingScore = 1))

        val state = makeDefaultPlayerStateWithRounds<DartsScorerRoundTheClock>(dartsThrown = listOf(roundOne))
        val statsPanel = GameStatisticsPanelRoundTheClock(CLOCK_TYPE_STANDARD)

        // [-]
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Avg darts") shouldBe "N/A"
        statsPanel.getValueForRow("Fewest darts") shouldBe "N/A"

        // [4, -]
        state.addDarts(listOf(makeDart(1, 1, startingScore = 1), makeDart(2, 0, startingScore = 2), makeDart(17, 1, startingScore = 2)))
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Avg darts") shouldBe 4.0
        statsPanel.getValueForRow("Fewest darts") shouldBe 4

        // [4, 3, 2]
        state.addDarts(listOf(makeDart(2, 1, startingScore = 2), makeDart(3, 0, startingScore = 3), makeDart(3, 1, startingScore = 3)))
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Avg darts") shouldBe 3.0
        statsPanel.getValueForRow("Fewest darts") shouldBe 2

        // [4, 3, 2, 1, 2]
        state.addDarts(listOf(makeDart(4, 1, startingScore = 4), makeDart(5, 0, startingScore = 5), makeDart(5, 1, startingScore = 5)))
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Avg darts") shouldBe 2.4
        statsPanel.getValueForRow("Fewest darts") shouldBe 1
    }

    @Test
    fun `Should correctly calculate longest streak`()
    {
        val roundOne = listOf(makeDart(5, 1, startingScore = 1), makeDart(1, 0, startingScore = 1), makeDart(20, 3, startingScore = 1))

        val state = makeDefaultPlayerStateWithRounds<DartsScorerRoundTheClock>(dartsThrown = listOf(roundOne))
        val statsPanel = GameStatisticsPanelRoundTheClock(CLOCK_TYPE_STANDARD)

        // [-]
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Best Streak") shouldBe 0

        // [4, 2] (streak of 1)
        state.addDarts(listOf(makeDart(1, 1, startingScore = 1), makeDart(2, 0, startingScore = 2), makeDart(2, 1, startingScore = 2)))
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Best Streak") shouldBe 1

        // [4, 2, 1, 1, -] (streak of 3)
        state.addDarts(listOf(makeDart(3, 1, startingScore = 3), makeDart(4, 1, startingScore = 4), makeDart(5, 0, startingScore = 5)))
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Best Streak") shouldBe 3

        // [4, 2, 1, 1, 2, 1, 1, 1] (streak of 4)
        state.addDarts(listOf(makeDart(5, 1, startingScore = 5), makeDart(6, 1, startingScore = 6), makeDart(7, 1, startingScore = 7), makeDart(8, 1, startingScore = 8)))
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Best Streak") shouldBe 4
    }

    @Test
    fun `Should take into account the gameParams when calculating best streak`()
    {
        val roundOne = listOf(makeDart(1, 1, startingScore = 1), makeDart(1, 2, startingScore = 1), makeDart(2, 1, startingScore = 2))

        val state = makeDefaultPlayerStateWithRounds<DartsScorerRoundTheClock>(dartsThrown = listOf(roundOne))
        val statsPanel = GameStatisticsPanelRoundTheClock(CLOCK_TYPE_DOUBLES)

        // [2, -]
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Best Streak") shouldBe 1

        // [2, 2, 1, -] (streak of 2)
        state.addDarts(listOf(makeDart(2, 2, startingScore = 2), makeDart(3, 2, startingScore = 3), makeDart(4, 3, startingScore = 4)))
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Best Streak") shouldBe 2
    }

    @Test
    fun `Should calculate brucey stats correctly`()
    {
        val roundOne = listOf(makeDart(5, 1, startingScore = 1), makeDart(1, 1, startingScore = 1), makeDart(17, 3, startingScore = 2))

        val state = makeDefaultPlayerStateWithRounds<DartsScorerRoundTheClock>(dartsThrown = listOf(roundOne))
        val statsPanel = GameStatisticsPanelRoundTheClock(CLOCK_TYPE_STANDARD)

        // [-]
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Brucey chances") shouldBe 0
        statsPanel.getValueForRow("Bruceys executed") shouldBe 0

        // [4, 2] (streak of 1)
        state.addDarts(listOf(makeDart(2, 1, startingScore = 2), makeDart(3, 1, startingScore = 3), makeDart(4, 1, startingScore = 4), makeDart(20, 1, startingScore = 5)))
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Brucey chances") shouldBe 1
        statsPanel.getValueForRow("Bruceys executed") shouldBe 0

        // [4, 2, 1, 1, -] (streak of 3)
        state.addDarts(listOf(makeDart(5, 1, startingScore = 5), makeDart(6, 1, startingScore = 6), makeDart(7, 1, startingScore = 7), makeDart(8, 1, startingScore = 8)))
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Brucey chances") shouldBe 2
        statsPanel.getValueForRow("Bruceys executed") shouldBe 1
    }

    @Test
    fun `Should put a finished streak into the correct breakdown category`()
    {
        val missRound = listOf(makeDart(5, 1, startingScore = 1), makeDart(1, 0, startingScore = 1), makeDart(20, 1, startingScore = 1))
        val hitRound = listOf(makeDart(5, 1, startingScore = 1), makeDart(1, 0, startingScore = 1), makeDart(1, 1, startingScore = 1))

        //3
        var state = makeDefaultPlayerStateWithRounds<DartsScorerRoundTheClock>(dartsThrown = listOf(hitRound))
        val statsPanel = GameStatisticsPanelRoundTheClock(CLOCK_TYPE_STANDARD)
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(mapOf("2 - 3" to 1))

        //6
        state = state.copy(darts = mutableListOf(missRound, hitRound))
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(mapOf("4 - 6" to 1))

        //9
        state = state.copy(darts = mutableListOf(missRound, missRound, hitRound))
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(mapOf("7 - 10" to 1))

        //12
        state = state.copy(darts = mutableListOf(missRound, missRound, missRound, hitRound))
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(mapOf("11 - 15" to 1))

        //18
        state = state.copy(darts = mutableListOf(missRound, missRound, missRound, missRound, missRound, hitRound))
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(mapOf("16 - 20" to 1))

        //21
        state = state.copy(darts = mutableListOf(missRound, missRound, missRound, missRound, missRound, missRound, hitRound))
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(mapOf("21+" to 1))
    }
}