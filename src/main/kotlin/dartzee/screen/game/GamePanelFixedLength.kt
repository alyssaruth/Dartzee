package dartzee.screen.game

import dartzee.db.GameEntity
import dartzee.game.state.AbstractPlayerState
import dartzee.screen.game.scorer.AbstractDartsScorer
import dartzee.utils.doesHighestWin
import dartzee.utils.setFinishingPositions

abstract class GamePanelFixedLength<
    S : AbstractDartsScorer<PlayerState>,
    PlayerState : AbstractPlayerState<PlayerState>
>(parent: AbstractDartsGameScreen, game: GameEntity, totalPlayers: Int) :
    DartsGamePanel<S, PlayerState>(parent, game, totalPlayers) {
    abstract val totalRounds: Int
    val highestWins = doesHighestWin(game.gameType)

    fun finishRound() {
        if (currentRoundNumber == totalRounds) {
            handlePlayerFinish()
        }

        turnFinished()
    }

    override fun turnFinished() {
        currentPlayerNumber = getNextPlayerNumber(currentPlayerNumber)
        if (getActiveCount() > 0) {
            nextTurn()
        } else {
            finishGame()
        }
    }

    private fun finishGame() {
        // Get the participants sorted by score so we can assign finishing positions
        setFinishingPositions(getParticipants().map { it.participant }, gameEntity)

        getPlayerStates().forEach { updateWinAchievement(it.wrappedParticipant) }

        allPlayersFinished()

        parentWindow.startNextGameIfNecessary()
    }

    override fun getFinishingPositionFromPlayersRemaining() = -1
}
