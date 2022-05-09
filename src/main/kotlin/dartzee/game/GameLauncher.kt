package dartzee.game

import dartzee.core.util.DialogUtil
import dartzee.dartzee.DartzeeRuleDto
import dartzee.db.DartsMatchEntity
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.game.state.IWrappedParticipant
import dartzee.logging.CODE_LOAD_ERROR
import dartzee.screen.ScreenCache
import dartzee.screen.game.DartsGameScreen
import dartzee.screen.game.dartzee.DartzeeMatchScreen
import dartzee.screen.game.golf.GolfMatchScreen
import dartzee.screen.game.rtc.RoundTheClockMatchScreen
import dartzee.screen.game.x01.X01MatchScreen
import dartzee.utils.InjectedThings.logger
import dartzee.utils.insertDartzeeRules

data class GameLaunchParams(
    val players: List<PlayerEntity>,
    val gameType: GameType,
    val gameParams: String,
    val pairMode: Boolean,
    val dartzeeDtos: List<DartzeeRuleDto>? = null
) {
    fun teamCount(): Int = if (pairMode) players.chunked(2).size else players.size
}

class GameLauncher
{
    fun launchNewMatch(match: DartsMatchEntity, params: GameLaunchParams)
    {
        val game = GameEntity.factoryAndSave(match)
        val participants = insertNewGameEntities(game.rowId, params)

        val scrn = factoryMatchScreen(match, participants)

        val panel = scrn.addGameToMatch(game)
        panel.startNewGame(participants)
    }

    fun launchNewGame(params: GameLaunchParams)
    {
        //Create and save a game
        val gameEntity = GameEntity.factoryAndSave(params.gameType, params.gameParams)

        val participants = insertNewGameEntities(gameEntity.rowId, params)

        //Construct the screen and factory a tab
        val scrn = DartsGameScreen(gameEntity, params.teamCount())
        scrn.isVisible = true
        scrn.gamePanel.startNewGame(participants)
    }

    private fun insertNewGameEntities(gameId: String, params: GameLaunchParams): List<IWrappedParticipant>
    {
        insertDartzeeRules(gameId, params.dartzeeDtos)

        return prepareParticipants(gameId, params)
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
        val participants = loadParticipants(gameEntity.rowId)
        val scrn = DartsGameScreen(gameEntity, participants.size)
        scrn.isVisible = true

        //Now try to load the game
        try
        {
            scrn.gamePanel.loadGame(participants)
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

        val firstGame = allGames.first()
        val lastGame = allGames[allGames.size - 1]

        val match = DartsMatchEntity().retrieveForId(matchId)
        match!!.cacheMetadataFromGame(lastGame)

        val scrn = factoryMatchScreen(match, loadParticipants(firstGame.rowId))

        try
        {
            allGames.forEach {
                val participants = loadParticipants(it.rowId)
                val panel = scrn.addGameToMatch(it)
                panel.loadGame(participants)
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

    private fun factoryMatchScreen(match: DartsMatchEntity, participants: List<IWrappedParticipant>) =
        when (match.gameType)
        {
            GameType.X01 -> X01MatchScreen(match, participants)
            GameType.ROUND_THE_CLOCK -> RoundTheClockMatchScreen(match, participants)
            GameType.GOLF -> GolfMatchScreen(match, participants)
            GameType.DARTZEE -> DartzeeMatchScreen(match, participants)
        }
}