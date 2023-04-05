package dartzee.ai

import dartzee.bean.PresentationDartboard
import dartzee.core.util.MathsUtil
import dartzee.logging.CODE_SIMULATION_FINISHED
import dartzee.logging.CODE_SIMULATION_STARTED
import dartzee.`object`.SegmentType
import dartzee.utils.InjectedThings.logger
import java.util.*

private const val SCORING_DARTS_TO_THROW = 20000
private const val DOUBLE_DARTS_TO_THROW = 20000

object DartsAiSimulator
{
    fun runSimulation(model: DartsAiModel, dartboard: PresentationDartboard): SimulationWrapper
    {
        logger.info(CODE_SIMULATION_STARTED, "Simulating scoring and doubles throws")

        val scoringPoints = throwScoringDarts(model)
        val hmPointToCount = scoringPoints.groupBy { dartboard.interpretPoint(it) }.mapValues { it.value.size }
        val scoringSegments = scoringPoints.map { it.segment }

        val totalScore = scoringSegments.sumOf { it.getTotal() }
        val misses = scoringSegments.count { it.getTotal() == 0 }
        val trebles = scoringSegments.count { it.type == SegmentType.TREBLE && it.score == model.scoringDart }

        val avgScore = totalScore.toDouble() / SCORING_DARTS_TO_THROW
        val missPercent = MathsUtil.getPercentage(misses, SCORING_DARTS_TO_THROW)
        val treblePercent = MathsUtil.getPercentage(trebles, SCORING_DARTS_TO_THROW)

        val doubles = throwAtDoubles(model)

        logger.info(CODE_SIMULATION_FINISHED, "Finished simulating throws")

        val doublePercent = MathsUtil.getPercentage(doubles, DOUBLE_DARTS_TO_THROW)
        return SimulationWrapper(avgScore, missPercent, doublePercent, treblePercent, hmPointToCount)
    }

    private fun throwScoringDarts(model: DartsAiModel) =
        (0 until SCORING_DARTS_TO_THROW).map {
            model.throwScoringDart()
        }

    private fun throwAtDoubles(model: DartsAiModel): Double
    {
        val rand = Random()

        return (0 until DOUBLE_DARTS_TO_THROW).fold(0.0) { hits, _ ->
            val doubleToAimAt = rand.nextInt(20) + 1

            val segment = model.throwAtDouble(doubleToAimAt).segment
            if (segment.score == doubleToAimAt && segment.type == SegmentType.DOUBLE) {
                hits + 1
            } else {
                hits
            }
        }
    }
}