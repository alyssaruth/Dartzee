package dartzee.screen.game.rtc

import dartzee.game.ClockType
import dartzee.game.RoundTheClockConfig
import dartzee.game.state.ClockPlayerState
import dartzee.helper.insertPlayer
import dartzee.helper.makeClockPlayerState
import dartzee.helper.makeDart
import dartzee.screen.game.AbstractGameStatisticsPanelTest
import dartzee.screen.game.getValueForRow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestGameStatisticsPanelRoundTheClock: AbstractGameStatisticsPanelTest<ClockPlayerState, GameStatisticsPanelRoundTheClock>()
{
    override fun factoryStatsPanel() = GameStatisticsPanelRoundTheClock(RoundTheClockConfig(ClockType.Standard, true).toJson())

    override fun makePlayerState(): ClockPlayerState
    {
        val roundOne = listOf(makeDart(5, 1, startingScore = 1), makeDart(1, 1, startingScore = 1), makeDart(2, 3, startingScore = 2))
        val roundTwo = listOf(makeDart(3, 1, startingScore = 3), makeDart(4, 1, startingScore = 4), makeDart(5, 1, startingScore = 5), makeDart(6, 0, startingScore = 6))

        return makeClockPlayerState(completedRounds = listOf(roundOne, roundTwo))
    }

    @Test
    fun `Most darts thrown should include unfinished rounds`()
    {
        val roundOne = listOf(makeDart(5, 1, startingScore = 1), makeDart(1, 1, startingScore = 1), makeDart(3, 1, startingScore = 2))

        val state = makeClockPlayerState(completedRounds = listOf(roundOne))
        val statsPanel = factoryStatsPanel()

        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Most darts") shouldBe 2

        state.addCompletedRound(listOf(makeDart(15, 1, startingScore = 2), makeDart(17, 1, startingScore = 2), makeDart(2, 0, startingScore = 2)))
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Most darts") shouldBe 4
    }

    @Test
    fun `Should separate out participants when calculating most fewest and avg darts`()
    {
        val roundOne = listOf(makeDart(5, 1, startingScore = 1), makeDart(1, 0, startingScore = 1), makeDart(1, 3, startingScore = 1))
        val roundOneOther = listOf(makeDart(1, 1, startingScore = 1), makeDart(2, 0, startingScore = 2), makeDart(17, 1, startingScore = 2))

        val player = insertPlayer()
        val stateOne = makeClockPlayerState(player = player, completedRounds = listOf(roundOne))
        val stateTwo = makeClockPlayerState(player = player, completedRounds = listOf(roundOneOther))

        val statsPanel = factoryStatsPanel()
        statsPanel.showStats(listOf(stateOne, stateTwo))

        statsPanel.getValueForRow("Most darts") shouldBe 3
        statsPanel.getValueForRow("Avg darts") shouldBe 2.0
        statsPanel.getValueForRow("Fewest darts") shouldBe 1
    }

    @Test
    fun `Should calculate avg and fewest darts correctly, ignoring unfinished rounds`()
    {
        val roundOne = listOf(makeDart(5, 1, startingScore = 1), makeDart(1, 0, startingScore = 1), makeDart(20, 3, startingScore = 1))

        val state = makeClockPlayerState(completedRounds = listOf(roundOne))
        val statsPanel = factoryStatsPanel()

        // [-]
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Avg darts") shouldBe "N/A"
        statsPanel.getValueForRow("Fewest darts") shouldBe "N/A"

        // [4, -]
        state.addCompletedRound(listOf(makeDart(1, 1, startingScore = 1), makeDart(2, 0, startingScore = 2), makeDart(17, 1, startingScore = 2)))
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Avg darts") shouldBe 4.0
        statsPanel.getValueForRow("Fewest darts") shouldBe 4

        // [4, 3, 2]
        state.addCompletedRound(listOf(makeDart(2, 1, startingScore = 2), makeDart(3, 0, startingScore = 3), makeDart(3, 1, startingScore = 3)))
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Avg darts") shouldBe 3.0
        statsPanel.getValueForRow("Fewest darts") shouldBe 2

        // [4, 3, 2, 1, 2]
        state.addCompletedRound(listOf(makeDart(4, 1, startingScore = 4), makeDart(5, 0, startingScore = 5), makeDart(5, 1, startingScore = 5)))
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Avg darts") shouldBe 2.4
        statsPanel.getValueForRow("Fewest darts") shouldBe 1
    }

    @Test
    fun `Should correctly calculate longest streak`()
    {
        val roundOne = listOf(makeDart(5, 1, startingScore = 1), makeDart(1, 0, startingScore = 1), makeDart(20, 3, startingScore = 1))

        val state = makeClockPlayerState(completedRounds = listOf(roundOne))
        val statsPanel = factoryStatsPanel()

        // [-]
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Best Streak") shouldBe 0

        // [4, 2] (streak of 1)
        state.addCompletedRound(listOf(makeDart(1, 1, startingScore = 1), makeDart(2, 0, startingScore = 2), makeDart(2, 1, startingScore = 2)))
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Best Streak") shouldBe 1

        // [4, 2, 1, 1, -] (streak of 3)
        state.addCompletedRound(listOf(makeDart(3, 1, startingScore = 3), makeDart(4, 1, startingScore = 4), makeDart(5, 0, startingScore = 5)))
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Best Streak") shouldBe 3

        // [4, 2, 1, 1, 2, 1, 1, 1] (streak of 4)
        state.addCompletedRound(listOf(makeDart(5, 1, startingScore = 5), makeDart(6, 1, startingScore = 6), makeDart(7, 1, startingScore = 7), makeDart(8, 1, startingScore = 8)))
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Best Streak") shouldBe 4
    }

    @Test
    fun `Should take into account the gameParams when calculating best streak`()
    {
        val roundOne = listOf(makeDart(1, 1, startingScore = 1), makeDart(1, 2, startingScore = 1), makeDart(2, 1, startingScore = 2))

        val state = makeClockPlayerState(completedRounds = listOf(roundOne))
        val statsPanel = GameStatisticsPanelRoundTheClock(RoundTheClockConfig(ClockType.Doubles, true).toJson())

        // [2, -]
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Best Streak") shouldBe 1

        // [2, 2, 1, -] (streak of 2)
        state.addCompletedRound(listOf(makeDart(2, 2, startingScore = 2), makeDart(3, 2, startingScore = 3), makeDart(4, 3, startingScore = 4)))
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Best Streak") shouldBe 2
    }

    @Test
    fun `Should calculate brucey stats correctly`()
    {
        val roundOne = listOf(makeDart(5, 1, startingScore = 1), makeDart(1, 1, startingScore = 1), makeDart(17, 3, startingScore = 2))

        val state = makeClockPlayerState(completedRounds = listOf(roundOne))
        val statsPanel = factoryStatsPanel()

        // [-]
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Brucey chances") shouldBe 0
        statsPanel.getValueForRow("Bruceys executed") shouldBe 0

        // [4, 2] (streak of 1)
        state.addCompletedRound(listOf(makeDart(2, 1, startingScore = 2), makeDart(3, 1, startingScore = 3), makeDart(4, 1, startingScore = 4), makeDart(20, 1, startingScore = 5)))
        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Brucey chances") shouldBe 1
        statsPanel.getValueForRow("Bruceys executed") shouldBe 0

        // [4, 2, 1, 1, -] (streak of 3)
        state.addCompletedRound(listOf(makeDart(5, 1, startingScore = 5), makeDart(6, 1, startingScore = 6), makeDart(7, 1, startingScore = 7), makeDart(8, 1, startingScore = 8)))
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
        var state = makeClockPlayerState(completedRounds = listOf(hitRound))
        val statsPanel = factoryStatsPanel()
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(mapOf("2 - 3" to 1))

        //6
        state = makeClockPlayerState(completedRounds = listOf(missRound, hitRound))
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(mapOf("4 - 6" to 1))

        //9
        state = state.copy(completedRounds = mutableListOf(missRound, missRound, hitRound))
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(mapOf("7 - 10" to 1))

        //12
        state = state.copy(completedRounds = mutableListOf(missRound, missRound, missRound, hitRound))
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(mapOf("11 - 15" to 1))

        //18
        state = state.copy(completedRounds = mutableListOf(missRound, missRound, missRound, missRound, missRound, hitRound))
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(mapOf("16 - 20" to 1))

        //21
        state = state.copy(completedRounds = mutableListOf(missRound, missRound, missRound, missRound, missRound, missRound, hitRound))
        statsPanel.showStats(listOf(state))
        statsPanel.shouldHaveBreakdownState(mapOf("21+" to 1))
    }
}