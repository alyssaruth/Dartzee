package dartzee.screen.game

import dartzee.achievements.AbstractAchievement
import dartzee.core.util.getSqlDateNow
import dartzee.db.DartsMatchEntity
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.db.PlayerEntity
import dartzee.screen.ScreenCache
import dartzee.utils.insertDartzeeRules
import java.awt.BorderLayout
import javax.swing.JTabbedPane
import javax.swing.SwingConstants
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

class DartsMatchScreen(val match: DartsMatchEntity, players: List<PlayerEntity>):
        AbstractDartsGameScreen(match.getPlayerCount(), match.gameType), ChangeListener
{
    private val matchPanel = MatchSummaryPanel(match)
    private val tabbedPane = JTabbedPane(SwingConstants.TOP)
    val hmGameIdToTab = mutableMapOf<String, DartsGamePanel<*, *>>()

    init
    {
        contentPane.add(tabbedPane, BorderLayout.CENTER)

        tabbedPane.addTab("Match", matchPanel)
        tabbedPane.addChangeListener(this)

        matchPanel.init(players)

        title = match.getMatchDesc()
    }

    override fun getScreenHeight() = super.getScreenHeight() + 30

    fun addGameToMatch(game: GameEntity): DartsGamePanel<*, *>
    {
        //Cache this screen in ScreenCache
        val gameId = game.rowId
        ScreenCache.addDartsGameScreen(gameId, this)

        //Initialise some basic properties of the tab, such as visibility of components etc
        val tab = DartsGamePanel.factory(this, game)
        tab.initBasic(match.getPlayerCount())

        matchPanel.addGameTab(tab)

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

        //Insert dartzee rules if applicable
        val priorGamePanel = hmGameIdToTab.values.first()
        if (priorGamePanel is GamePanelDartzee)
        {
            insertDartzeeRules(nextGame, priorGamePanel.dtos)
        }

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
        if (selectedTab is DartsGamePanel<*, *>)
        {
            val title = selectedTab.gameTitle
            setTitle(title)
        }
        else
        {
            title = match.getMatchDesc()
        }
    }
}