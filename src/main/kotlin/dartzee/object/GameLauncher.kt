package dartzee.`object`

import dartzee.core.util.Debug
import dartzee.core.util.DialogUtil
import dartzee.dartzee.DartzeeRuleDto
import dartzee.db.DartsMatchEntity
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.screen.ScreenCache
import dartzee.screen.game.DartsGameScreen
import dartzee.screen.game.DartsMatchScreen
import dartzee.utils.insertDartzeeRules

object GameLauncher
{
    fun launchNewMatch(match: DartsMatchEntity, dartzeeDtos: List<DartzeeRuleDto>? = null)
    {
        val scrn = DartsMatchScreen(match, match.players)

        val game = GameEntity.factoryAndSave(match)

        insertDartzeeRules(game, dartzeeDtos)

        val panel = scrn.addGameToMatch(game)
        panel.startNewGame(match.players)
    }

    fun launchNewGame(players: List<PlayerEntity>, gameType: Int, gameParams: String, dartzeeDtos: List<DartzeeRuleDto>? = null)
    {
        //Create and save a game
        val gameEntity = GameEntity.factoryAndSave(gameType, gameParams)

        insertDartzeeRules(gameEntity, dartzeeDtos)

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
            DialogUtil.showError("Game $gameId does not exist.")
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