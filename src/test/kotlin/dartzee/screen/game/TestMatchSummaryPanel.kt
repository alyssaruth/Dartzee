package dartzee.screen.game

import dartzee.game.state.IWrappedParticipant
import dartzee.game.state.X01PlayerState
import dartzee.getRows
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.`object`.Dart
import dartzee.screen.game.x01.GameStatisticsPanelX01
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class TestMatchSummaryPanel : AbstractTest()
{
    @Test
    fun `Should correctly assign scorers and then reuse for existing participants`()
    {
        val panel = makeMatchSummaryPanel()

        val p1 = insertPlayer(name = "Alyssa")
        val p2 = insertPlayer(name = "Leah")
        val p3 = insertPlayer(name = "Qwi")
        val p4 = insertPlayer(name = "Natalie")

        val alyssaAndLeah = makeTeam(p1, p2)
        val qwiAndNatalie = makeTeam(p3, p4)

        panel.addParticipant(1, alyssaAndLeah)
        panel.addParticipant(1, qwiAndNatalie)
        panel.scorersOrdered.size shouldBe 2

        val leahAndAlyssa = makeTeam(p2, p1)
        val natalieAndQwi = makeTeam(p4, p3)
        panel.addParticipant(2, natalieAndQwi)
        panel.addParticipant(2, leahAndAlyssa)
        panel.scorersOrdered.size shouldBe 2

        val firstScorer = panel.scorersOrdered[0]
        val rows = firstScorer.model.getRows(4)
        rows[0] shouldBe listOf(1L, alyssaAndLeah, alyssaAndLeah, alyssaAndLeah)
        rows[1] shouldBe listOf(2L, leahAndAlyssa, leahAndAlyssa, leahAndAlyssa)

        val secondScorer = panel.scorersOrdered[1]
        val otherRows = secondScorer.model.getRows(4)
        otherRows[0] shouldBe listOf(1L, qwiAndNatalie, qwiAndNatalie, qwiAndNatalie)
        otherRows[1] shouldBe listOf(2L, natalieAndQwi, natalieAndQwi, natalieAndQwi)
    }
    private fun MatchSummaryPanel<X01PlayerState>.addParticipant(localId: Long, participant: IWrappedParticipant)
    {
        addParticipant(localId, X01PlayerState(501, participant))
    }

    @Test
    fun `Should add listeners to player states, and update stats using all of them`()
    {
        val statsPanel = mockk<GameStatisticsPanelX01>(relaxed = true)
        val matchPanel = makeMatchSummaryPanel(statsPanel = statsPanel)

        val gameOne = makeX01GamePanel()
        val gameTwo = makeX01GamePanel()

        matchPanel.addGameTab(gameOne)
        gameOne.getPlayerStates().forEach { matchPanel.addParticipant(gameOne.gameEntity.localId, it) }
        matchPanel.finaliseScorers(mockk(relaxed = true))

        matchPanel.addGameTab(gameTwo)
        gameTwo.getPlayerStates().forEach { matchPanel.addParticipant(gameTwo.gameEntity.localId, it) }

        // Now trigger a state change for one of the player states
        val state = gameOne.getPlayerStates().first()
        state.dartThrown(Dart(20, 1))

        val expectedStates = gameOne.getPlayerStates() + gameTwo.getPlayerStates()
        verify { statsPanel.showStats(expectedStates) }
    }

    @Test
    fun `Should return all participants as a flat list`()
    {
        val gamePanelOne = makeX01GamePanel()
        val gamePanelTwo = makeX01GamePanel()

        val panel = makeMatchSummaryPanel()
        panel.addGameTab(gamePanelOne)
        panel.addGameTab(gamePanelTwo)

        panel.getAllParticipants().shouldHaveSize(2)
    }
}