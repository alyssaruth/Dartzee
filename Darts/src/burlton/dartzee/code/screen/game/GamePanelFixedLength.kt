package burlton.dartzee.code.screen.game

import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.screen.Dartboard
import burlton.dartzee.code.screen.game.scorer.DartsScorer
import burlton.dartzee.code.utils.doesHighestWin
import burlton.dartzee.code.utils.setFinishingPositions

abstract class GamePanelFixedLength<S : DartsScorer, D: Dartboard>(parent: AbstractDartsGameScreen, game: GameEntity):
        DartsGamePanel<S, D>(parent, game)
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
        setFinishingPositions(hmPlayerNumberToParticipant.values.toList(), gameEntity)

        updateScorersWithFinishingPositions()

        allPlayersFinished()

        parentWindow?.startNextGameIfNecessary()
    }

    override fun getFinishingPositionFromPlayersRemaining(): Int
    {
        //Finishing positions are determined at the end
        return -1
    }
}