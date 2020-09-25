package dartzee.screen.game.dartzee

import dartzee.`object`.Dart
import dartzee.dartzee.DartzeeRoundResult
import dartzee.game.state.DartzeePlayerState
import dartzee.helper.makeDartzeePlayerState
import dartzee.helper.makeDartzeePlayerStateForName
import dartzee.helper.makeRoundResultEntities
import dartzee.screen.game.AbstractGameStatisticsPanelTest
import dartzee.screen.game.getValueForRow
import io.kotlintest.shouldBe
import org.junit.Test

class TestGameStatisticsPanelDartzee: AbstractGameStatisticsPanelTest<DartzeePlayerState, GameStatisticsPanelDartzee>()
{
    override fun makePlayerState(): DartzeePlayerState
    {
        //Initial score of 26
        val firstRound = listOf(Dart(20, 1), Dart(5, 1), Dart(1, 1))

        //Score of 83
        val secondRound = listOf(Dart(19, 3), Dart(17, 1), Dart(3, 1))
        val secondResult = DartzeeRoundResult(4, true, 57)

        //Score of 42
        val thirdRound = listOf(Dart(20, 3), Dart(20, 0), Dart(5, 1))
        val thirdResult = DartzeeRoundResult(1, false, -41)

        return makeDartzeePlayerStateForName(completedRounds = listOf(firstRound, secondRound, thirdRound), roundResults = listOf(secondResult, thirdResult))
    }

    override fun factoryStatsPanel() = GameStatisticsPanelDartzee()

    @Test
    fun `Should correctly identify the peak score for a player`()
    {
        val statsPanel = GameStatisticsPanelDartzee()
        val state = makePlayerState()
        statsPanel.showStats(listOf(state))

        statsPanel.getValueForRow("Peak Score") shouldBe 83
    }

    @Test
    fun `Should correctly identify the highest, lowest and avg round scores`()
    {
        val statsPanel = GameStatisticsPanelDartzee()
        val state = makePlayerState()
        statsPanel.showStats(listOf(state))

        statsPanel.getValueForRow("Highest Round") shouldBe 57
        statsPanel.getValueForRow("Lowest Round") shouldBe -41
        statsPanel.getValueForRow("Avg Round") shouldBe 8.0
    }

    @Test
    fun `Should correctly identify the longest streak`()
    {
        val statsPanel = GameStatisticsPanelDartzee()
        val state = makePlayerState()
        statsPanel.showStats(listOf(state))

        statsPanel.getValueForRow("Longest Streak") shouldBe 1

        val extraResults = listOf(DartzeeRoundResult(2, true, 20),
            DartzeeRoundResult(3, true, 5))

        val resultEntities = makeRoundResultEntities(*extraResults.toTypedArray())
        resultEntities.forEach { state.addRoundResult(it) }

        statsPanel.showStats(listOf(state))
        statsPanel.getValueForRow("Longest Streak") shouldBe 2
    }

    @Test
    fun `Should correctly identify the hardest rule that has been passed`()
    {
        val statsPanel = GameStatisticsPanelDartzee()
        val state = makePlayerState()
        statsPanel.showStats(listOf(state))

        statsPanel.getValueForRow("Hardest Rule") shouldBe 4

        val extraResults = listOf(DartzeeRoundResult(5, true, 20),
            DartzeeRoundResult(6, false, 5))

        val resultEntities = makeRoundResultEntities(*extraResults.toTypedArray())
        resultEntities.forEach { state.addRoundResult(it) }
        statsPanel.showStats(listOf(state))

        statsPanel.getValueForRow("Hardest Rule") shouldBe 5
    }

}