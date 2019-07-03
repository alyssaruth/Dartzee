package burlton.dartzee.code.screen.game

import burlton.core.code.util.Debug
import burlton.dartzee.code.achievements.AbstractAchievement
import burlton.dartzee.code.db.DartsMatchEntity
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.screen.ScreenCache
import burlton.desktopcore.code.util.DialogUtil
import javax.swing.WindowConstants

/**
 * DartsGameScreen
 * Simple screen which wraps up either a single game panel, or multiple tabs for a match.
 */
class DartsGameScreen : AbstractDartsGameScreen()
{
    private var gamePanel: DartsGamePanel<out DartsScorer>? = null

    init
    {
        setSize(880, 675)

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
        contentPane.add(tab)
        gamePanel = tab
        isVisible = true

        return tab
    }

    override fun fireAppearancePreferencesChanged()
    {
        gamePanel!!.fireAppearancePreferencesChanged()
    }

    override fun achievementUnlocked(gameId: String, playerId: String, achievement: AbstractAchievement)
    {
        gamePanel!!.achievementUnlocked(playerId, achievement)
    }



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

            val scrn = DartsMatchScreen()
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
