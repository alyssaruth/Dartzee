package dartzee.game

import dartzee.core.util.DialogUtil
import dartzee.dartzee.DartzeeRuleDto
import dartzee.db.DartsMatchEntity
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.logging.CODE_LOAD_ERROR
import dartzee.screen.ScreenCache
import dartzee.screen.game.DartsGameScreen
import dartzee.screen.game.dartzee.DartzeeMatchScreen
import dartzee.screen.game.golf.GolfMatchScreen
import dartzee.screen.game.rtc.RoundTheClockMatchScreen
import dartzee.screen.game.x01.X01MatchScreen
import dartzee.utils.InjectedThings.logger
import dartzee.utils.insertDartzeeRules

class GameLauncher
{
    fun launchNewMatch(match: DartsMatchEntity, dartzeeDtos: List<DartzeeRuleDto>? = null)
    {
        val scrn = factoryMatchScreen(match)

        val game = GameEntity.factoryAndSave(match)

        insertDartzeeRules(game.rowId, dartzeeDtos)

        val panel = scrn.addGameToMatch(game)
        panel.startNewGame(match.players)
    }

    fun launchNewGame(players: List<PlayerEntity>, gameType: GameType, gameParams: String, dartzeeDtos: List<DartzeeRuleDto>? = null)
    {
        //Create and save a game
        val gameEntity = GameEntity.factoryAndSave(gameType, gameParams)

        insertDartzeeRules(gameEntity.rowId, dartzeeDtos)

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
            logger.error(CODE_LOAD_ERROR, "Failed to load Game ${gameEntity.rowId}", t)
            DialogUtil.showError("Failed to load Game #${gameEntity.localId}")
            scrn.dispose()
            ScreenCache.removeDartsGameScreen(scrn)
        }
    }

    private fun loadAndDisplayMatch(matchId: String, originalGameId: String)
    {
        val allGames = GameEntity.retrieveGamesForMatch(matchId)
        val lastGame = allGames[allGames.size - 1]

        val match = DartsMatchEntity().retrieveForId(matchId)
        match!!.cacheMetadataFromGame(lastGame)

        val scrn = factoryMatchScreen(match)

        try
        {
            allGames.forEach {
                val panel = scrn.addGameToMatch(it)
                panel.loadGame()
            }

            scrn.displayGame(originalGameId)
        }
        catch (t: Throwable)
        {
            logger.error(CODE_LOAD_ERROR, "Failed to load Match $matchId", t)
            DialogUtil.showError("Failed to load Match #${match.localId}")
            scrn.dispose()
            ScreenCache.removeDartsGameScreen(scrn)
        }

        scrn.updateTotalScores()
    }

    private fun factoryMatchScreen(match: DartsMatchEntity) =
        when (match.gameType)
        {
            GameType.X01 -> X01MatchScreen(match)
            GameType.ROUND_THE_CLOCK -> RoundTheClockMatchScreen(match)
            GameType.GOLF -> GolfMatchScreen(match)
            GameType.DARTZEE -> DartzeeMatchScreen(match)
        }
}