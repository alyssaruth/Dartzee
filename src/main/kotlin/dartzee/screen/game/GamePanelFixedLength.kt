package dartzee.screen.game

import dartzee.db.GameEntity
import dartzee.game.state.AbstractPlayerState
import dartzee.screen.TempDartboardBase
import dartzee.screen.game.scorer.AbstractDartsScorer
import dartzee.utils.doesHighestWin
import dartzee.utils.setFinishingPositions

abstract class GamePanelFixedLength<S : AbstractDartsScorer<PlayerState>, D: TempDartboardBase, PlayerState: AbstractPlayerState<PlayerState>>(
    parent: AbstractDartsGameScreen,
    game: GameEntity,
    totalPlayers: Int): DartsGamePanel<S, D, PlayerState>(parent, game, totalPlayers)
{
    abstract val totalRounds: Int
    val highestWins = doesHighestWin(game.gameType)

    fun finishRound()
    {
        if (currentRoundNumber == totalRounds)
        {
            handlePlayerFinish()
        }

        currentPlayerNumber = getNextPlayerNumber(currentPlayerNumber)
        if (getActiveCount() > 0)
        {
            nextTurn()
        }
        else
        {
            finishGame()
        }
    }

    private fun finishGame()
    {
        //Get the participants sorted by score so we can assign finishing positions
        setFinishingPositions(getParticipants().map { it.participant }, gameEntity)

        allPlayersFinished()

        parentWindow.startNextGameIfNecessary()
    }

    override fun getFinishingPositionFromPlayersRemaining(): Int
    {
        //Finishing positions are determined at the end
        return -1
    }
}