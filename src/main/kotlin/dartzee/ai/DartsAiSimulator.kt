package dartzee.ai

import dartzee.core.obj.HashMapCount
import dartzee.logging.CODE_SIMULATION_FINISHED
import dartzee.logging.CODE_SIMULATION_STARTED
import dartzee.screen.Dartboard
import dartzee.utils.InjectedThings.logger
import java.awt.Point
import java.util.*

private const val SCORING_DARTS_TO_THROW = 20000
private const val DOUBLE_DARTS_TO_THROW = 20000

object DartsAiSimulator
{
    fun runSimulation(model: DartsAiModel, dartboard: Dartboard): SimulationWrapper
    {
        logger.info(CODE_SIMULATION_STARTED, "Simulating scoring and doubles throws")

        val hmPointToCount = HashMapCount<Point>()

        var totalScore = 0.0
        var missPercent = 0.0
        var treblePercent = 0.0

        repeat(SCORING_DARTS_TO_THROW) {
            val pt = model.throwScoringDart(dartboard)
            dartboard.rationalisePoint(pt)

            hmPointToCount.incrementCount(pt)

            val dart = dartboard.convertPointToDart(pt, false)
            totalScore += dart.getTotal()

            if (dart.getTotal() == 0)
            {
                missPercent++
            }

            if (dart.multiplier == 3 && dart.score == model.scoringDart)
            {
                treblePercent++
            }
        }

        val avgScore = totalScore / SCORING_DARTS_TO_THROW
        missPercent = 100 * missPercent / SCORING_DARTS_TO_THROW
        treblePercent = 100 * treblePercent / SCORING_DARTS_TO_THROW

        var doublesHit = 0.0
        val rand = Random()
        repeat(DOUBLE_DARTS_TO_THROW) {
            val doubleToAimAt = rand.nextInt(20) + 1

            val pt = model.throwAtDouble(doubleToAimAt, dartboard)
            val dart = dartboard.convertPointToDart(pt, true)

            if (dart.isDouble() && dart.score == doubleToAimAt)
            {
                doublesHit++
            }
        }

        logger.info(CODE_SIMULATION_FINISHED, "Finished simulating throws")

        val doublePercent = 100 * doublesHit / DOUBLE_DARTS_TO_THROW
        return SimulationWrapper(avgScore, missPercent, doublePercent, treblePercent, hmPointToCount)
    }
}