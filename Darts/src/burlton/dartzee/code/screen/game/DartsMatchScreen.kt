package burlton.dartzee.code.screen.game

import burlton.dartzee.code.achievements.AbstractAchievement
import burlton.dartzee.code.db.DartsMatchEntity
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.ParticipantEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.screen.ScreenCache
import burlton.dartzee.code.screen.game.scorer.DartsScorer
import burlton.desktopcore.code.util.getSqlDateNow
import java.awt.BorderLayout
import javax.swing.JTabbedPane
import javax.swing.SwingConstants
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

class DartsMatchScreen(var match: DartsMatchEntity, players: MutableList<PlayerEntity>): AbstractDartsGameScreen(match.getPlayerCount()), ChangeListener
{
    private val matchPanel = MatchSummaryPanel()
    private val tabbedPane = JTabbedPane(SwingConstants.TOP)
    val hmGameIdToTab = mutableMapOf<String, DartsGamePanel<out DartsScorer>>()

    init
    {
        contentPane.add(tabbedPane, BorderLayout.CENTER)

        tabbedPane.addTab("Match", matchPanel)
        tabbedPane.addChangeListener(this)

        matchPanel.init(match, players)

        title = match.getMatchDesc()
    }

    override fun getScreenHeight() = 705

    fun addGameToMatch(game: GameEntity): DartsGamePanel<out DartsScorer>
    {
        //Cache this screen in ScreenCache
        val gameId = game.rowId
        ScreenCache.addDartsGameScreen(gameId, this)

        //Initialise some basic properties of the tab, such as visibility of components etc
        val tab = DartsGamePanel.factory(this, game)
        tab.initBasic(match.getPlayerCount())

        //Add the single game tab and set the parent window to be visible
        tabbedPane.addTab("#" + game.localId, tab)
        hmGameIdToTab[gameId] = tab
        isVisible = true

        return tab
    }

    fun addParticipant(localId: Long, participant: ParticipantEntity)
    {
        matchPanel.addParticipant(localId, participant)
    }

    fun updateTotalScores()
    {
        matchPanel.updateTotalScores()
    }

    override fun startNextGameIfNecessary()
    {
        updateTotalScores()

        if (match.isComplete())
        {
            match.dtFinish = getSqlDateNow()
            match.saveToDatabase()
            return
        }

        //Factory and save the next game
        val nextGame = GameEntity.factoryAndSave(match)
        val panel = addGameToMatch(nextGame)

        match.shufflePlayers()
        panel.startNewGame(match.players)
    }

    override fun achievementUnlocked(gameId: String, playerId: String, achievement: AbstractAchievement)
    {
        val tab = hmGameIdToTab[gameId]!!
        tab.achievementUnlocked(playerId, achievement)
    }

    override fun displayGame(gameId: String)
    {
        super.displayGame(gameId)

        val tab = hmGameIdToTab[gameId]
        tabbedPane.selectedComponent = tab
    }

    override fun fireAppearancePreferencesChanged()
    {
        hmGameIdToTab.values.forEach {
            it.fireAppearancePreferencesChanged()
        }
    }

    /**
     * ChangeListener
     */
    override fun stateChanged(e: ChangeEvent)
    {
        val sourceTabbedPane = e.source as JTabbedPane
        val selectedTab = sourceTabbedPane.selectedComponent
        if (selectedTab is DartsGamePanel<*>)
        {
            val title = selectedTab.gameTitle
            setTitle(title)

            if (selectedTab.pendingLoad)
            {
                selectedTab.loadGameInCatch()
            }
        }
        else
        {
            title = match.getMatchDesc()
        }
    }
}