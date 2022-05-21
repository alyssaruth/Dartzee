package dartzee.ai

import dartzee.core.util.MathsUtil
import dartzee.logging.CODE_SIMULATION_FINISHED
import dartzee.logging.CODE_SIMULATION_STARTED
import dartzee.screen.Dartboard
import dartzee.utils.InjectedThings.logger
import java.util.*

private const val SCORING_DARTS_TO_THROW = 20000
private const val DOUBLE_DARTS_TO_THROW = 20000

object DartsAiSimulator
{
    fun runSimulation(model: DartsAiModel, dartboard: Dartboard): SimulationWrapper
    {
        logger.info(CODE_SIMULATION_STARTED, "Simulating scoring and doubles throws")

        val scoringPoints = throwScoringDarts(model, dartboard)
        val hmPointToCount = scoringPoints.groupBy { it }.mapValues { it.value.size }
        val scoringDarts = scoringPoints.map { dartboard.convertPointToDart(it, false) }

        val totalScore = scoringDarts.sumOf { it.getTotal() }
        val misses = scoringDarts.count { it.getTotal() == 0 }
        val trebles = scoringDarts.count { it.isTreble() && it.score == model.scoringDart }

        val avgScore = totalScore.toDouble() / SCORING_DARTS_TO_THROW
        val missPercent = MathsUtil.getPercentage(misses, SCORING_DARTS_TO_THROW)
        val treblePercent = MathsUtil.getPercentage(trebles, SCORING_DARTS_TO_THROW)

        val doubles = throwAtDoubles(model, dartboard)

        logger.info(CODE_SIMULATION_FINISHED, "Finished simulating throws")

        val doublePercent = MathsUtil.getPercentage(doubles, DOUBLE_DARTS_TO_THROW)
        return SimulationWrapper(avgScore, missPercent, doublePercent, treblePercent, hmPointToCount)
    }

    private fun throwScoringDarts(model: DartsAiModel, dartboard: Dartboard) =
        (0 until SCORING_DARTS_TO_THROW).map {
            val pt = model.throwScoringDart(dartboard)
            dartboard.rationalisePoint(pt)
            pt
        }

    private fun throwAtDoubles(model: DartsAiModel, dartboard: Dartboard): Double
    {
        val rand = Random()

        return (0 until DOUBLE_DARTS_TO_THROW).fold(0.0) { hits, _ ->
            val doubleToAimAt = rand.nextInt(20) + 1

            val pt = model.throwAtDouble(doubleToAimAt, dartboard)
            val dart = dartboard.convertPointToDart(pt, true)
            if (dart.score == doubleToAimAt && dart.isDouble()) {
                hits + 1
            } else {
                hits
            }
        }
    }
}