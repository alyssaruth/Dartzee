package burlton.dartzee.code.ai

import burlton.core.code.util.Debug
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.db.GAME_TYPE_X01
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.screen.Dartboard
import burlton.dartzee.code.utils.isBust
import burlton.dartzee.code.utils.shouldStopForMercyRule

private const val X01 = 501

/**
 * Simulate a single game of X01 for an AI
 */
class DartsSimulationX01(dartboard: Dartboard, player: PlayerEntity, model: AbstractDartsModel) : AbstractDartsSimulation(dartboard, player, model)
{
    //Transient things
    protected var startingScore = -1
    protected var currentScore = -1

    override val gameParams = "$X01"
    override val gameType = GAME_TYPE_X01

    override fun getTotalScore(): Int
    {
        val totalRounds = currentRound - 1
        return (totalRounds - 1) * 3 + dartsThrown.size
    }

    override fun shouldPlayCurrentRound(): Boolean
    {
        return currentScore > 0
    }

    override fun resetVariables()
    {
        super.resetVariables()
        startingScore = X01
        currentScore = X01
    }

    override fun startRound()
    {
        //Starting a new round. Need to keep track of what we started on so we can reset if we bust.
        startingScore = currentScore
        resetRound()

        model.throwX01Dart(currentScore, dartboard)
    }

    private fun finishedRound()
    {
        hmRoundNumberToDarts[currentRound] = dartsThrown

        //If we've bust, then reset the current score back
        if (isBust(currentScore, dartsThrown.last()))
        {
            currentScore = startingScore
        }

        Debug.appendBanner("Round $currentRound", logging)
        Debug.append("StartingScore [$startingScore]", logging)
        Debug.append("Darts [$dartsThrown]", logging)
        Debug.append("CurrentScore [$currentScore]", logging)

        currentRound++
    }

    override fun dartThrown(dart: Dart)
    {
        dartsThrown.add(dart)
        dart.startingScore = currentScore

        val dartTotal = dart.getTotal()
        currentScore -= dartTotal

        if (currentScore <= 1
                || dartsThrown.size == 3
                || shouldStopForMercyRule(model, startingScore, currentScore))
        {
            finishedRound()
        }
        else
        {
            model.throwX01Dart(currentScore, dartboard)
        }
    }
}
