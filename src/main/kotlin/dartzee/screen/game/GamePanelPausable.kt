package dartzee.screen.game

import dartzee.core.util.getSqlDateNow
import dartzee.db.GameEntity
import dartzee.game.state.AbstractPlayerState
import dartzee.preferences.Preferences
import dartzee.screen.game.scorer.AbstractDartsScorerPausable
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.preferenceService
import javax.swing.SwingUtilities

abstract class GamePanelPausable<
    S : AbstractDartsScorerPausable<PlayerState>,
    PlayerState : AbstractPlayerState<PlayerState>,
>(parent: AbstractDartsGameScreen, game: GameEntity, totalPlayers: Int) :
    DartsGamePanel<S, PlayerState>(parent, game, totalPlayers) {
    private var aiShouldPause = false

    /** Abstract methods */
    abstract fun currentPlayerHasFinished(): Boolean

    override fun saveDartsAndProceed() {
        commitRound()

        // This player has finished. The game isn't necessarily over though...
        if (currentPlayerHasFinished()) {
            handlePlayerFinish()
        }

        turnFinished()
    }

    override fun turnFinished() {
        currentPlayerNumber = getNextPlayerNumber(currentPlayerNumber)

        val activePlayers = getActiveCount()
        if (activePlayers > 1 || (activePlayers == 1 && totalPlayers == 1)) {
            // We always keep going if there's more than 1 active person in play
            nextTurn()
        } else if (activePlayers == 1) {
            // Finish the game and set the last player's finishing position if we haven't already
            finishGameIfNecessary()

            if (!getCurrentScorer().getPaused()) {
                nextTurn()
            }
        } else {
            allPlayersFinished()
        }
    }

    override fun shouldAIStop(): Boolean {
        if (aiShouldPause) {
            aiShouldPause = false
            return true
        }

        return false
    }

    private fun finishGameIfNecessary() {
        if (gameEntity.isFinished()) {
            return
        }

        getCurrentPlayerState()
            .setParticipantFinishPosition(getFinishingPositionFromPlayersRemaining())

        gameEntity.dtFinish = getSqlDateNow()
        gameEntity.saveToDatabase()

        parentWindow.startNextGameIfNecessary()

        // Display this player's result. If they're an AI and we have the preference, then
        // automatically play on.
        updateActivePlayer()
        if (
            !getCurrentPlayerState().isHuman() && preferenceService.get(Preferences.aiAutoContinue)
        ) {
            getCurrentScorer().toggleResume()
        }

        if (InjectedThings.partyMode) {
            allPlayersFinished()
        }
    }

    fun pauseLastPlayer() {
        if (!getCurrentPlayerState().isHuman() && cpuThread != null) {
            aiShouldPause = true
            cpuThread!!.join()
        }

        // Now the player has definitely stopped, reset the round.
        // InvokeLater so we go after any remaining UI updates that the cpu thread may have fired
        SwingUtilities.invokeLater {
            resetRound()

            // Set the current round number back to the previous round
            currentRoundNumber--

            dartboard.stopListening()
        }
    }

    fun unpauseLastPlayer() {
        aiShouldPause = false
        nextTurn()
    }
}
