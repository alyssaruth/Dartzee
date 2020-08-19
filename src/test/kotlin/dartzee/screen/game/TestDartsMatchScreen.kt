package dartzee.screen.game

import com.github.alexburlton.swingtest.getChild
import dartzee.achievements.x01.AchievementX01BestFinish
import dartzee.core.helper.verifyNotCalled
import dartzee.db.DartsMatchEntity
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.db.PlayerImageEntity
import dartzee.game.state.DefaultPlayerState
import dartzee.helper.*
import dartzee.screen.ScreenCache
import dartzee.screen.game.scorer.DartsScorerX01
import dartzee.screen.game.x01.GamePanelX01
import dartzee.screen.game.x01.MatchStatisticsPanelX01
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import javax.swing.JTabbedPane

class TestDartsMatchScreen: AbstractTest()
{
    @Test
    fun `Should pop up achievement unlocks on the correct tab`()
    {
        val scrn = setUpMatchScreen()

        val g1 = insertGame()
        val g2 = insertGame()

        val panelOne = scrn.addGameToMatch(g1)
        val panelTwo = scrn.addGameToMatch(g2)

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

        val tab = scrn.addGameToMatch(game)
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

        scrn.addGameToMatch(g1)
        scrn.addGameToMatch(g2)

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
        val panel = scrn.addGameToMatch(g)

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
        val panel1 = scrn.addGameToMatch(g1)
        val panel2 = scrn.addGameToMatch(g2)

        scrn.displayGame(g1.rowId)
        tabbedPane.selectedComponent shouldBe panel1

        scrn.displayGame(g2.rowId)
        tabbedPane.selectedComponent shouldBe panel2
    }

    @Test
    fun `Should pass certain fns through to the match summary panel`()
    {
        val matchSummaryPanel = mockk<MatchSummaryPanel<DefaultPlayerState<DartsScorerX01>>>(relaxed = true)
        val scrn = setUpMatchScreen(matchSummaryPanel = matchSummaryPanel)

        scrn.updateTotalScores()
        verify { matchSummaryPanel.updateTotalScores() }

        val pt = insertParticipant()
        scrn.addParticipant(500L, pt)
        verify { matchSummaryPanel.addParticipant(500L, pt) }
    }

    @Test
    fun `Should update total scores one last time and mark the match as complete if no more games need to be played`()
    {
        val matchEntity = mockk<DartsMatchEntity>(relaxed = true)
        every { matchEntity.isComplete() } returns true

        val matchSummaryPanel = mockk<MatchSummaryPanel<DefaultPlayerState<DartsScorerX01>>>(relaxed = true)
        val scrn = setUpMatchScreen(match = matchEntity, matchSummaryPanel = matchSummaryPanel)
        scrn.startNextGameIfNecessary()

        verify { matchSummaryPanel.updateTotalScores() }
        verify { matchEntity.dtFinish = any() }
        verify { matchEntity.saveToDatabase(any()) }
    }

    @Test
    fun `Should start a new game if the match is not yet complete`()
    {
        val p1 = insertPlayer()
        val p2 = insertPlayer()

        val match = insertDartsMatch(gameParams = "501")
        match.players = mutableListOf(p1, p2)

        val scrn = setUpMatchScreen(match = match)
        val firstGame = insertGame()
        scrn.addGameToMatch(firstGame)

        scrn.startNextGameIfNecessary()

        //Players should have been shuffled
        match.players.shouldContainExactly(p2, p1)

        //Game panel should have been added and had a game kicked off
        val gamePanel = scrn.getChild<GamePanelX01> { it.gameEntity != firstGame }
        verify { gamePanel.startNewGame(listOf(p2, p1)) }
    }

    private fun setUpMatchScreen(match: DartsMatchEntity = insertDartsMatch(gameParams = "501"),
                                 matchSummaryPanel: MatchSummaryPanel<DefaultPlayerState<DartsScorerX01>> = MatchSummaryPanel(match, MatchStatisticsPanelX01(match.gameParams))): FakeMatchScreen
    {
        PlayerImageEntity.createPresets()
        return FakeMatchScreen(match, listOf(insertPlayer(), insertPlayer()), matchSummaryPanel)
    }
}

private class FakeMatchScreen(match: DartsMatchEntity,
                              players: List<PlayerEntity>,
                              matchSummaryPanel: MatchSummaryPanel<DefaultPlayerState<DartsScorerX01>>):
        DartsMatchScreen<DefaultPlayerState<DartsScorerX01>>(matchSummaryPanel, match, players)
{
    override fun factoryGamePanel(parent: AbstractDartsGameScreen, game: GameEntity): GamePanelX01
    {
        val panel = mockk<GamePanelX01>(relaxed = true)
        every { panel.gameEntity } returns game
        every { panel.gameTitle } returns "${game.localId}"
        return panel
    }
}