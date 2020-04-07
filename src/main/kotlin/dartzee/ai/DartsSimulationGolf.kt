package dartzee.ai

import dartzee.`object`.Dart
import dartzee.core.util.Debug
import dartzee.db.GameType
import dartzee.db.PlayerEntity
import dartzee.screen.Dartboard

private const val ROUNDS = 18

class DartsSimulationGolf(dartboard: Dartboard, player: PlayerEntity, model: AbstractDartsModel) : AbstractDartsSimulation(dartboard, player, model)
{
    override val gameType = GameType.GOLF
    override val gameParams = "$ROUNDS"
    var score = 0

    override fun shouldPlayCurrentRound(): Boolean
    {
        return currentRound <= ROUNDS
    }

    override fun resetVariables()
    {
        super.resetVariables()
        score = 0
    }

    override fun getTotalScore() = score

    override fun startRound()
    {
        resetRound()

        val dartNo = dartsThrown.size + 1
        model.throwGolfDart(currentRound, dartNo, dartboard)
    }

    private fun finishedRound()
    {
        hmRoundNumberToDarts[currentRound] = dartsThrown

        val drt = dartsThrown.last()
        val roundScore = drt.getGolfScore(currentRound)
        score += roundScore

        Debug.appendBanner("Round $currentRound", logging)
        Debug.append("Darts [$dartsThrown]", logging)
        Debug.append("Score [$roundScore]", logging)
        Debug.append("Total Score [${getTotalScore()}]", logging)

        currentRound++
    }

    override fun dartThrown(dart: Dart)
    {
        dartsThrown.add(dart)

        val noDarts = dartsThrown.size
        val stopThreshold = model.getStopThresholdForDartNo(noDarts)

        if (noDarts == 3 || dart.getGolfScore(currentRound) <= stopThreshold)
        {
            finishedRound()
        }
        else
        {
            val dartNo = dartsThrown.size + 1
            model.throwGolfDart(currentRound, dartNo, dartboard)
        }
    }
}
