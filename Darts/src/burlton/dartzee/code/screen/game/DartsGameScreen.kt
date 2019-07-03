package burlton.dartzee.code.screen.game

import burlton.core.code.obj.SuperHashMap
import burlton.core.code.util.Debug
import burlton.dartzee.code.achievements.AbstractAchievement
import burlton.dartzee.code.db.DartsMatchEntity
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.ParticipantEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.screen.ScreenCache
import burlton.desktopcore.code.util.DialogUtil
import burlton.desktopcore.code.util.getSqlDateNow
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import javax.swing.JFrame
import javax.swing.JTabbedPane
import javax.swing.SwingConstants
import javax.swing.WindowConstants
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

/**
 * DartsGameScreen
 * Simple screen which wraps up either a single game panel, or multiple tabs for a match.
 */
open class DartsGameScreen : JFrame(), WindowListener, ChangeListener
{
    private var match: DartsMatchEntity? = null
    var haveLostFocus = false

    private val matchPanel = MatchSummaryPanel()
    private val tabbedPane = JTabbedPane(SwingConstants.TOP)
    private val hmGameIdToTab = SuperHashMap<String, DartsGamePanel<out DartsScorer>>()

    fun getGamePanel(): DartsGamePanel<out DartsScorer>
    {
        if (isMatch())
        {
            Debug.stackTrace("Calling getGamePanel when this is a multi-game screen.")
        }

        return hmGameIdToTab.values.first()
    }

    private fun isMatch(): Boolean = match != null

    init
    {
        setSize(880, 675)
        contentPane.add(tabbedPane, BorderLayout.CENTER)

        tabbedPane.addTab("Match", matchPanel)
        tabbedPane.addChangeListener(this)

        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        addWindowListener(this)
    }

    /**
     * Init
     */
    fun initSingleGame(game: GameEntity, totalPlayers: Int): DartsGamePanel<out DartsScorer>
    {
        //Re-size the screen based on how many players there are
        setScreenSize(totalPlayers)

        //Cache this screen in ScreenCache
        val gameId = game.rowId
        ScreenCache.addDartsGameScreen(gameId, this)

        //Initialise some basic properties of the tab, such as visibility of components etc
        val tab = DartsGamePanel.factory(this, game.gameType)
        tab.initBasic(game, totalPlayers)

        title = tab.gameTitle

        //Add the single game tab and set visible
        contentPane.remove(tabbedPane)
        contentPane.add(tab)
        hmGameIdToTab[gameId] = tab
        isVisible = true

        return tab
    }

    fun initMatch(match: DartsMatchEntity, players: MutableList<PlayerEntity>)
    {
        this.match = match

        matchPanel.init(match, players)

        title = match.getMatchDesc()

        setScreenSize(match.getPlayerCount())
    }

    private fun updateTotalScores()
    {
        matchPanel.updateTotalScores()
    }

    private fun addGameToMatch(game: GameEntity): DartsGamePanel<out DartsScorer>
    {
        //Cache this screen in ScreenCache
        val gameId = game.rowId
        ScreenCache.addDartsGameScreen(gameId, this)

        //Initialise some basic properties of the tab, such as visibility of components etc
        val tab = DartsGamePanel.factory(this, game.gameType)
        tab.initBasic(game, match!!.getPlayerCount())

        //Add the single game tab and set the parent window to be visible
        tabbedPane.addTab("#" + game.localId, tab)
        hmGameIdToTab[gameId] = tab
        isVisible = true

        return tab
    }

    private fun setScreenSize(playerCount: Int)
    {
        val newSize = Dimension(520 + (playerCount * SCORER_WIDTH), 675 + if (isMatch()) 30 else 0)
        size = newSize
        isResizable = false
    }

    /**
     * Update the match panel. Only do this if we're a match screen, otherwise we haven't initted the relevant table model
     */
    fun addParticipant(localId: Long, participant: ParticipantEntity)
    {
        if (isMatch())
        {
            matchPanel.addParticipant(localId, participant)
        }
    }

    fun fireAppearancePreferencesChanged()
    {
        hmGameIdToTab.values.forEach {
            it.fireAppearancePreferencesChanged()
        }
    }

    /**
     * Hook for when a GameId has been clicked and the screen is already visible.
     */
    fun displayGame(gameId: String)
    {
        toFront()
        state = Frame.NORMAL

        if (isMatch())
        {
            val tab = hmGameIdToTab[gameId]
            tabbedPane.selectedComponent = tab
        }
    }

    /**
     * Called when the next game should start.
     */
    fun startNextGameIfNecessary()
    {
        if (!isMatch())
        {
            return
        }

        updateTotalScores()

        if (match!!.isComplete())
        {
            match!!.dtFinish = getSqlDateNow()
            match!!.saveToDatabase()
            return
        }

        //Factory and save the next game
        val nextGame = GameEntity.factoryAndSave(match!!)
        val panel = addGameToMatch(nextGame)

        match!!.shufflePlayers()
        panel.startNewGame(match!!.players)
    }

    open fun achievementUnlocked(gameId: String, playerId: String, achievement: AbstractAchievement)
    {
        if (isMatch())
        {
            val tab = hmGameIdToTab[gameId]!!
            tab.achievementUnlocked(playerId, achievement)
        }
        else
        {
            getGamePanel().achievementUnlocked(playerId, achievement)
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
            title = match!!.getMatchDesc()
        }
    }

    /**
     * WindowListener
     */
    override fun windowClosed(arg0: WindowEvent)
    {
        ScreenCache.removeDartsGameScreen(this)
    }
    override fun windowDeactivated(arg0: WindowEvent)
    {
        haveLostFocus = true
    }

    override fun windowActivated(arg0: WindowEvent){}
    override fun windowClosing(arg0: WindowEvent){}
    override fun windowDeiconified(arg0: WindowEvent){}
    override fun windowIconified(arg0: WindowEvent){}
    override fun windowOpened(arg0: WindowEvent){}

    companion object
    {
        /**
         * Static methods
         */
        fun launchNewGame(players: List<PlayerEntity>, gameType: Int, gameParams: String)
        {
            //Create and save a game
            val gameEntity = GameEntity.factoryAndSave(gameType, gameParams)

            //Construct the screen and factory a tab
            val scrn = DartsGameScreen()
            val panel = scrn.initSingleGame(gameEntity, players.size)
            panel.startNewGame(players)
        }

        fun launchNewMatch(match: DartsMatchEntity)
        {
            val scrn = DartsGameScreen()
            scrn.initMatch(match, match.players)

            val game = GameEntity.factoryAndSave(match)
            val panel = scrn.addGameToMatch(game)
            panel.startNewGame(match.players)
        }

        fun loadAndDisplayGame(gameId: String)
        {
            val existingScreen = ScreenCache.getDartsGameScreen(gameId)
            if (existingScreen != null)
            {
                existingScreen.displayGame(gameId)
                return
            }

            //Screen isn't currently visible, so look for the game on the DB
            val gameEntity = GameEntity().retrieveForId(gameId, false)
            if (gameEntity == null)
            {
                DialogUtil.showError("Game #$gameId does not exist.")
                return
            }

            val matchId = gameEntity.dartsMatchId
            if (matchId.isEmpty())
            {
                loadAndDisplaySingleGame(gameEntity)
            }
            else
            {
                loadAndDisplayMatch(matchId, gameId)
            }
        }

        private fun loadAndDisplaySingleGame(gameEntity: GameEntity)
        {
            //We've found a game, so construct a screen and initialise it
            val playerCount = gameEntity.getParticipantCount()
            val scrn = DartsGameScreen()
            val panel = scrn.initSingleGame(gameEntity, playerCount)

            //Now try to load the game
            try
            {
                panel.loadGame()
            }
            catch (t: Throwable)
            {
                Debug.stackTrace(t)
                DialogUtil.showError("Failed to load Game #" + gameEntity.rowId)
                scrn.dispose()
                ScreenCache.removeDartsGameScreen(scrn)
            }

        }

        private fun loadAndDisplayMatch(matchId: String, originalGameId: String)
        {
            val allGames = GameEntity.retrieveGamesForMatch(matchId)

            val firstGame = allGames.first()
            val lastGame = allGames[allGames.size - 1]

            val match = DartsMatchEntity().retrieveForId(matchId)
            match!!.cacheMetadataFromGame(lastGame)

            val scrn = DartsGameScreen()
            scrn.initMatch(match, firstGame.retrievePlayersVector())

            try
            {
                allGames.forEach {
                    val panel = scrn.addGameToMatch(it)
                    when (it.rowId)
                    {
                        originalGameId -> panel.loadGame()
                        else -> panel.preLoad()
                    }
                }

                scrn.displayGame(originalGameId)
            }
            catch (t: Throwable)
            {
                Debug.stackTrace(t)
                DialogUtil.showError("Failed to load Match #$matchId")
                scrn.dispose()
                ScreenCache.removeDartsGameScreen(scrn)
            }

            scrn.updateTotalScores()
        }
    }
}
