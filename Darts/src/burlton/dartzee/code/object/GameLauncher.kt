package burlton.dartzee.code.`object`

import burlton.core.code.util.Debug
import burlton.dartzee.code.db.DartsMatchEntity
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.screen.ScreenCache
import burlton.dartzee.code.screen.game.DartsGameScreen
import burlton.dartzee.code.screen.game.DartsMatchScreen
import burlton.desktopcore.code.util.DialogUtil

object GameLauncher
{
    fun launchNewMatch(match: DartsMatchEntity)
    {
        val scrn = DartsMatchScreen(match, match.players)

        val game = GameEntity.factoryAndSave(match)
        val panel = scrn.addGameToMatch(game)
        panel.startNewGame(match.players)
    }

    fun launchNewGame(players: List<PlayerEntity>, gameType: Int, gameParams: String)
    {
        //Create and save a game
        val gameEntity = GameEntity.factoryAndSave(gameType, gameParams)

        //Construct the screen and factory a tab
        val scrn = DartsGameScreen(gameEntity, players.size)
        scrn.isVisible = true
        scrn.gamePanel.startNewGame(players)
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
        val scrn = DartsGameScreen(gameEntity, playerCount)
        scrn.isVisible = true

        //Now try to load the game
        try
        {
            scrn.gamePanel.loadGame()
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

        val scrn = DartsMatchScreen(match, firstGame.retrievePlayersVector())

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