package dartzee.screen.game

import com.github.alyssaburlton.swingtest.getChild
import dartzee.achievements.x01.AchievementX01BestFinish
import dartzee.core.helper.verifyNotCalled
import dartzee.core.util.DateStatics
import dartzee.db.DartsMatchEntity
import dartzee.db.GameEntity
import dartzee.db.PlayerImageEntity
import dartzee.game.loadParticipants
import dartzee.game.state.X01PlayerState
import dartzee.helper.AbstractTest
import dartzee.helper.insertDartsMatch
import dartzee.helper.insertGame
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.helper.makeX01PlayerState
import dartzee.screen.ScreenCache
import dartzee.screen.game.x01.GamePanelX01
import dartzee.screen.game.x01.MatchStatisticsPanelX01
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.awt.Dimension
import javax.swing.JTabbedPane
import javax.swing.SwingUtilities

class TestDartsMatchScreen: AbstractTest()
{
    @Test
    fun `Should pop up achievement unlocks on the correct tab`()
    {
        val scrn = setUpMatchScreen()

        val g1 = insertGame()
        val g2 = insertGame()

        val panelOne = scrn.addGameToMatchOnEdt(g1)
        val panelTwo = scrn.addGameToMatchOnEdt(g2)

        val achievement = AchievementX01BestFinish()
        scrn.achievementUnlocked(g2.rowId, "player", achievement)

        verifyNotCalled { panelOne.achievementUnlocked(any(), any()) }
        verify { panelTwo.achievementUnlocked("player", achievement) }
    }
    
    @Test
    fun `Should add games to the tabbed pane, as well as the screen cache`()
    {
        val scrn = setUpMatchScreen()
        val game = insertGame()

        val tab = scrn.addGameToMatchOnEdt(game)
        tab.shouldBeInstanceOf<GamePanelX01>()

        ScreenCache.getDartsGameScreen(game.rowId) shouldBe scrn
    }

    @Test
    fun `Should update title based on selected tab`()
    {
        val match = insertDartsMatch(gameParams = "501")
        val scrn = setUpMatchScreen(match)

        val g1 = insertGame()
        val g2 = insertGame()

        scrn.addGameToMatchOnEdt(g1)
        scrn.addGameToMatchOnEdt(g2)

        val tabbedPane = scrn.getChild<JTabbedPane>()
        tabbedPane.selectedIndex = 1
        scrn.title shouldBe g1.localId.toString()

        tabbedPane.selectedIndex = 2
        scrn.title shouldBe g2.localId.toString()

        tabbedPane.selectedIndex = 0
        scrn.title shouldBe match.getMatchDesc()
    }

    @Test
    fun `Should fire appearance updates to child tabs`()
    {
        val scrn = setUpMatchScreen()

        val g = insertGame()
        val panel = scrn.addGameToMatchOnEdt(g)

        scrn.fireAppearancePreferencesChanged()
        verify { panel.fireAppearancePreferencesChanged() }
    }

    @Test
    fun `Should flick to the correct tab when told to display a certain game`()
    {
        val scrn = setUpMatchScreen()
        val tabbedPane = scrn.getChild<JTabbedPane>()

        val g1 = insertGame()
        val g2 = insertGame()
        val panel1 = scrn.addGameToMatchOnEdt(g1)
        val panel2 = scrn.addGameToMatchOnEdt(g2)

        scrn.displayGame(g1.rowId)
        tabbedPane.selectedComponent shouldBe panel1

        scrn.displayGame(g2.rowId)
        tabbedPane.selectedComponent shouldBe panel2
    }

    @Test
    fun `Should pass certain fns through to the match summary panel`()
    {
        val matchSummaryPanel = mockk<MatchSummaryPanel<X01PlayerState>>(relaxed = true)
        val scrn = setUpMatchScreen(matchSummaryPanel = matchSummaryPanel)


        val state = makeX01PlayerState()
        scrn.addParticipant(500L, state)
        verify { matchSummaryPanel.addParticipant(500L, state) }

        scrn.finaliseParticipants()
        verify { matchSummaryPanel.finaliseScorers(scrn) }
    }

    @Test
    fun `Should mark the match as complete if no more games need to be played`()
    {
        val match = insertDartsMatch(games = 1, gameParams = "501")
        val scrn = setUpMatchScreen(match = match)

        val firstGame = insertGame()
        val firstPanel = scrn.addGameToMatchOnEdt(firstGame)

        val pt = insertParticipant(finishingPosition = 1, playerId = insertPlayer().rowId)
        val state = makeX01PlayerState(participant = pt)
        every { firstPanel.getPlayerStates() } returns listOf(state)

        scrn.startNextGameIfNecessary()

        match.dtFinish shouldNotBe DateStatics.END_OF_TIME
    }

    @Test
    fun `Should start a new game if the match is not yet complete`()
    {
        val p1 = insertPlayer(name = "Amy")
        val p2 = insertPlayer(name = "Billie")
        val gameOneStates = listOf(p1, p2).map { makeX01PlayerState(player = it) }

        val match = insertDartsMatch(gameParams = "501")

        val scrn = setUpMatchScreen(match = match)
        val firstGame = insertGame(dartsMatchId = match.rowId, matchOrdinal = 1)

        val firstPanel = scrn.addGameToMatchOnEdt(firstGame)
        every { firstPanel.getPlayerStates() } returns gameOneStates

        scrn.startNextGameIfNecessaryOnEdt()

        //Game panel should have been added and had a game kicked off
        val gamePanel = scrn.getChild<GamePanelX01> { it.gameEntity != firstGame }
        verify { gamePanel.startNewGame(any()) }

        val gameTwo = gamePanel.gameEntity
        gameTwo.matchOrdinal shouldBe 2
        gameTwo.dartsMatchId shouldBe match.rowId

        val participants = loadParticipants(gameTwo.rowId)
        participants[0].getParticipantNameHtml(false) shouldBe "<html>Billie</html>"
        participants[1].getParticipantNameHtml(false) shouldBe "<html>Amy</html>"
    }

    @Test
    fun `Should not repack the screen after the first time`()
    {
        val match = insertDartsMatch(gameParams = "501")

        val scrn = setUpMatchScreen(match = match)
        scrn.packIfNecessary()

        // Player changes to their desired size...
        scrn.size = Dimension(1000, 1000)
        scrn.packIfNecessary()
        scrn.size.shouldBe(Dimension(1000, 1000))
    }

    private fun setUpMatchScreen(
        match: DartsMatchEntity = insertDartsMatch(gameParams = "501"),
        matchSummaryPanel: MatchSummaryPanel<X01PlayerState> = MatchSummaryPanel(match, MatchStatisticsPanelX01(match.gameParams))
    ): FakeMatchScreen
    {
        PlayerImageEntity().createPresets()
        return FakeMatchScreen(match, matchSummaryPanel)
    }
}

private class FakeMatchScreen(match: DartsMatchEntity,
                              matchSummaryPanel: MatchSummaryPanel<X01PlayerState>):
        DartsMatchScreen<X01PlayerState>(matchSummaryPanel, match)
{
    override fun factoryGamePanel(parent: AbstractDartsGameScreen, game: GameEntity, totalPlayers: Int): GamePanelX01
    {
        val panel = mockk<GamePanelX01>(relaxed = true)
        every { panel.gameEntity } returns game
        every { panel.gameTitle } returns "${game.localId}"
        return panel
    }

    fun addGameToMatchOnEdt(gameEntity: GameEntity): DartsGamePanel<*, *, *>
    {
        var panel: DartsGamePanel<*, *, *>? = null
        SwingUtilities.invokeAndWait {
            panel = addGameToMatch(gameEntity, 2)
        }

        return panel!!
    }

    fun startNextGameIfNecessaryOnEdt()
    {
        SwingUtilities.invokeAndWait { startNextGameIfNecessary() }
    }

}