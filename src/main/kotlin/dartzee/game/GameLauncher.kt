package dartzee.game

import dartzee.core.util.DialogUtil
import dartzee.db.DartsMatchEntity
import dartzee.db.GameEntity
import dartzee.game.state.IWrappedParticipant
import dartzee.logging.CODE_GAME_LAUNCHED
import dartzee.logging.CODE_LOAD_ERROR
import dartzee.logging.CODE_MATCH_LAUNCHED
import dartzee.logging.KEY_FROM_MATCH
import dartzee.logging.KEY_GAME_ID
import dartzee.logging.KEY_GAME_LOCAL_ID
import dartzee.logging.KEY_MATCH_ID
import dartzee.logging.KEY_MATCH_LOCAL_ID
import dartzee.screen.ScreenCache
import dartzee.screen.game.DartsGameScreen
import dartzee.screen.game.dartzee.DartzeeMatchScreen
import dartzee.screen.game.golf.GolfMatchScreen
import dartzee.screen.game.rtc.RoundTheClockMatchScreen
import dartzee.screen.game.x01.X01MatchScreen
import dartzee.utils.InjectedThings.logger
import dartzee.utils.insertDartzeeRules

class GameLauncher {
    fun launchNewMatch(match: DartsMatchEntity, params: GameLaunchParams) {
        val game = GameEntity.factoryAndSave(params, match)
        match.cacheMetadataFromGame(game)

        val participants = insertNewGameEntities(game.rowId, params)

        val scrn = factoryMatchScreen(match)

        val panel = scrn.addGameToMatch(game, participants.size)
        panel.startNewGame(participants)

        logger.info(
            CODE_MATCH_LAUNCHED,
            "Launched ${scrn.windowName}",
            KEY_MATCH_ID to match.rowId,
            KEY_MATCH_LOCAL_ID to match.localId
        )
    }

    fun launchNewGame(params: GameLaunchParams) {
        // Create and save a game
        val game = GameEntity.factoryAndSave(params)
        val participants = insertNewGameEntities(game.rowId, params)

        // Construct the screen and factory a tab
        val scrn = DartsGameScreen(game, participants)
        scrn.isVisible = true
        scrn.startNewGame()

        logger.info(
            CODE_GAME_LAUNCHED,
            "Launched ${scrn.windowName}",
            KEY_GAME_ID to game.rowId,
            KEY_GAME_LOCAL_ID to game.localId,
            KEY_FROM_MATCH to false
        )
    }

    private fun insertNewGameEntities(
        gameId: String,
        params: GameLaunchParams
    ): List<IWrappedParticipant> {
        insertDartzeeRules(gameId, params.dartzeeDtos)
        return prepareParticipants(gameId, params.players, params.pairMode)
    }

    fun loadAndDisplayGame(gameId: String) {
        val existingScreen = ScreenCache.getDartsGameScreen(gameId)
        if (existingScreen != null) {
            existingScreen.displayGame(gameId)
            return
        }

        // Screen isn't currently visible, so look for the game on the DB
        val gameEntity = GameEntity().retrieveForId(gameId, false)
        if (gameEntity == null) {
            DialogUtil.showErrorOLD("Game $gameId does not exist.")
            return
        }

        val matchId = gameEntity.dartsMatchId
        if (matchId.isEmpty()) {
            loadAndDisplaySingleGame(gameEntity)
        } else {
            loadAndDisplayMatch(matchId, gameId)
        }
    }

    private fun loadAndDisplaySingleGame(gameEntity: GameEntity) {
        // We've found a game, so construct a screen and initialise it
        val participants = loadParticipants(gameEntity.rowId)
        val scrn = DartsGameScreen(gameEntity, participants)
        scrn.isVisible = true

        // Now try to load the game
        try {
            scrn.gamePanel.loadGame(participants)
        } catch (t: Throwable) {
            logger.error(CODE_LOAD_ERROR, "Failed to load Game ${gameEntity.rowId}", t)
            DialogUtil.showErrorOLD("Failed to load Game #${gameEntity.localId}")
            scrn.dispose()
            ScreenCache.removeDartsGameScreen(scrn)
        }
    }

    private fun loadAndDisplayMatch(matchId: String, originalGameId: String) {
        val allGames = GameEntity.retrieveGamesForMatch(matchId)
        val lastGame = allGames[allGames.size - 1]

        val match = DartsMatchEntity().retrieveForId(matchId)
        match!!.cacheMetadataFromGame(lastGame)

        val scrn = factoryMatchScreen(match)

        try {
            allGames.forEach { game ->
                val participants = loadParticipants(game.rowId)
                val panel = scrn.addGameToMatch(game, participants.size)
                panel.loadGame(participants)
            }

            scrn.displayGame(originalGameId)
        } catch (t: Throwable) {
            logger.error(CODE_LOAD_ERROR, "Failed to load Match $matchId", t)
            DialogUtil.showErrorOLD("Failed to load Match #${match.localId}")
            scrn.dispose()
            ScreenCache.removeDartsGameScreen(scrn)
        }
    }

    private fun factoryMatchScreen(match: DartsMatchEntity) =
        when (match.gameType) {
            GameType.X01 -> X01MatchScreen(match)
            GameType.ROUND_THE_CLOCK -> RoundTheClockMatchScreen(match)
            GameType.GOLF -> GolfMatchScreen(match)
            GameType.DARTZEE -> DartzeeMatchScreen(match)
        }
}
