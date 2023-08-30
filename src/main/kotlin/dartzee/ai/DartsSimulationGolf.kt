package dartzee.ai

import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.`object`.Dart

private const val ROUNDS = 18

class DartsSimulationGolf(player: PlayerEntity, model: DartsAiModel) : AbstractDartsSimulation(player, model)
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
        val pt = model.throwGolfDart(currentRound, dartNo)
        dartThrown(pt)
    }

    private fun finishedRound()
    {
        confirmRound()

        val drt = dartsThrown.last()
        val roundScore = drt.getGolfScore(currentRound)
        score += roundScore

        currentRound++
    }

    override fun dartThrown(dart: Dart)
    {
        dartsThrown.add(dart)

        val noDarts = dartsThrown.size

        if (noDarts == 3 || dart.getGolfScore(currentRound) <= model.getStopThresholdForDartNo(noDarts))
        {
            finishedRound()
        }
        else
        {
            val dartNo = dartsThrown.size + 1
            val pt = model.throwGolfDart(currentRound, dartNo)
            dartThrown(pt)
        }
    }
}
