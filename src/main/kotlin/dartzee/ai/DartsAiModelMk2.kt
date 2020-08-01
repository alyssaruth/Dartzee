package dartzee.ai

import dartzee.`object`.Dart
import dartzee.`object`.SegmentType
import dartzee.`object`.getSegmentTypeForClockType
import dartzee.core.obj.HashMapCount
import dartzee.logging.CODE_AI_ERROR
import dartzee.logging.CODE_SIMULATION_FINISHED
import dartzee.logging.CODE_SIMULATION_STARTED
import dartzee.screen.Dartboard
import dartzee.screen.dartzee.DartzeeDartboard
import dartzee.screen.game.dartzee.SegmentStatus
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.logger
import dartzee.utils.generateRandomAngle
import dartzee.utils.getAngleForPoint
import dartzee.utils.translatePoint
import getPointForScore
import org.apache.commons.math3.distribution.NormalDistribution
import java.awt.Point
import java.util.*

data class DartsAiModelMk2(val standardDeviation: Double,
                           val standardDeviationDoubles: Double?,
                           val standardDeviationCentral: Double?,
                           val scoringDart: Int,
                           val hmScoreToDart: Map<Int, Dart>,
                           val mercyThreshold: Int,
                           val hmDartNoToSegmentType: Map<Int, SegmentType>,
                           val hmDartNoToStopThreshold: Map<Int, Int>,
                           val dartzeePlayStyle: DartzeePlayStyle)
{
    //Modelling
    private val mean = 0

    private val distribution = NormalDistribution(mean.toDouble(), standardDeviation)

    /**
     * X01
     */
    fun throwX01Dart(score: Int, dartboard: Dartboard)
    {
        val pt = getX01Dart(score, dartboard)
        dartboard.dartThrown(pt)
    }

    private fun getX01Dart(score: Int, dartboard: Dartboard): Point
    {
        //Check for a specific dart to aim for. It's possible to override any value for a specific AI strategy.
        val drtToAimAt = getOveriddenDartToAimAt(score)
        if (drtToAimAt != null)
        {
            val ptToAimAt = getPointForScore(drtToAimAt, dartboard)
            return throwDartAtPoint(ptToAimAt, dartboard)
        }

        //No overridden strategy, do the default thing
        if (score > 60)
        {
            return throwScoringDart(dartboard)
        }
        else
        {
            val defaultDrt = getDefaultDartToAimAt(score)
            val ptToAimAt = getPointForScore(defaultDrt, dartboard)
            return throwDartAtPoint(ptToAimAt, dartboard)
        }
    }

    fun throwScoringDart(dartboard: Dartboard): Point
    {
        val ptToAimAt = getScoringPoint(dartboard)
        return throwDartAtPoint(ptToAimAt, dartboard)
    }

    fun getScoringPoint(dartboard: Dartboard): Point
    {
        val segmentType = if (scoringDart == 25) SegmentType.DOUBLE else SegmentType.TREBLE
        return getPointForScore(scoringDart, dartboard, segmentType)
    }

    private fun getOveriddenDartToAimAt(score: Int) = hmScoreToDart[score]

    /**
     * Golf
     */
    fun throwGolfDart(targetHole: Int, dartNo: Int, dartboard: Dartboard)
    {
        val segmentTypeToAimAt = getSegmentTypeForDartNo(dartNo)
        val ptToAimAt = getPointForScore(targetHole, dartboard, segmentTypeToAimAt)
        val pt = throwDartAtPoint(ptToAimAt, dartboard)
        dartboard.dartThrown(pt)
    }

    private fun getDefaultSegmentType(dartNo: Int) = if (dartNo == 1) SegmentType.DOUBLE else SegmentType.TREBLE
    private fun getDefaultStopThreshold(dartNo: Int) = if (dartNo == 2) 3 else 2

    /**
     * Clock
     */
    fun throwClockDart(clockTarget: Int, clockType: String, dartboard: Dartboard)
    {
        val segmentType = getSegmentTypeForClockType(clockType)

        val ptToAimAt = getPointForScore(clockTarget, dartboard, segmentType)
        val pt = throwDartAtPoint(ptToAimAt, dartboard)
        dartboard.dartThrown(pt)
    }

    /**
     * Dartzee
     */
    fun throwDartzeeDart(dartsThrownSoFar: Int, dartboard: DartzeeDartboard, segmentStatus: SegmentStatus)
    {
        val aggressive = (dartsThrownSoFar < 2 || dartzeePlayStyle == DartzeePlayStyle.AGGRESSIVE)
        val ptToAimAt = InjectedThings.dartzeeAimCalculator.getPointToAimFor(dartboard, segmentStatus, aggressive)
        val pt = throwDartAtPoint(ptToAimAt, dartboard)
        dartboard.dartThrown(pt)
    }

    fun runSimulation(dartboard: Dartboard): SimulationWrapper
    {
        logger.info(CODE_SIMULATION_STARTED, "Simulating scoring and doubles throws")

        val hmPointToCount = HashMapCount<Point>()

        var totalScore = 0.0
        var missPercent = 0.0
        var treblePercent = 0.0

        for (i in 0 until SCORING_DARTS_TO_THROW)
        {
            val ptToAimAt = getScoringPoint(dartboard)

            val pt = throwDartAtPoint(ptToAimAt, dartboard)
            dartboard.rationalisePoint(pt)

            hmPointToCount.incrementCount(pt)

            val dart = dartboard.convertPointToDart(pt, false)
            totalScore += dart.getTotal()

            if (dart.getTotal() == 0)
            {
                missPercent++
            }

            if (dart.multiplier == 3 && dart.score == scoringDart)
            {
                treblePercent++
            }
        }

        val avgScore = totalScore / SCORING_DARTS_TO_THROW
        missPercent = 100 * missPercent / SCORING_DARTS_TO_THROW
        treblePercent = 100 * treblePercent / SCORING_DARTS_TO_THROW

        var doublesHit = 0.0
        val rand = Random()
        for (i in 0 until DOUBLE_DARTS_TO_THROW)
        {
            val doubleToAimAt = rand.nextInt(20) + 1

            val doublePtToAimAt = getPointForScore(doubleToAimAt, dartboard, SegmentType.DOUBLE)

            val pt = throwDartAtPoint(doublePtToAimAt, dartboard)
            val dart = dartboard.convertPointToDart(pt, true)

            if (dart.getTotal() == doubleToAimAt * 2 && dart.isDouble())
            {
                doublesHit++
            }
        }

        logger.info(CODE_SIMULATION_FINISHED, "Finished simulating throws")

        val doublePercent = 100 * doublesHit / DOUBLE_DARTS_TO_THROW
        return SimulationWrapper(avgScore, missPercent, doublePercent, treblePercent, hmPointToCount)
    }

    fun getSegmentTypeForDartNo(dartNo: Int) = hmDartNoToSegmentType.getOrDefault(dartNo, getDefaultSegmentType(dartNo))

    fun getStopThresholdForDartNo(dartNo: Int) = hmDartNoToStopThreshold.getOrDefault(dartNo, getDefaultStopThreshold(dartNo))

    fun throwDartAtPoint(pt: Point, dartboard: Dartboard): Point
    {
        if (standardDeviation == 0.0)
        {
            logger.error(CODE_AI_ERROR, "Gaussian model with SD of 0 - this shouldn't be possible!")
            return pt
        }

        val (radius, angle) = calculateRadiusAndAngle(pt, dartboard)

        return translatePoint(pt, radius, angle)
    }

    data class DistributionSample(val radius: Double, val theta: Double)
    fun calculateRadiusAndAngle(pt: Point, dartboard: Dartboard): DistributionSample
    {
        //Averaging logic
        val radius = sampleRadius(pt, dartboard)

        //Generate the angle
        val theta = generateAngle(pt, dartboard)
        val sanitisedAngle = sanitiseAngle(theta)

        return DistributionSample(radius, sanitisedAngle)
    }
    private fun sampleRadius(pt: Point, dartboard: Dartboard): Double
    {
        val distribution = getDistributionToUse(pt, dartboard)!!
        return distribution.sample()
    }
    private fun sanitiseAngle(angle: Double): Double
    {
        return when
        {
            angle < 0 -> angle + 360
            angle > 360 -> angle - 360
            else -> angle
        }
    }

    private fun getDistributionToUse(pt: Point, dartboard: Dartboard) =
        if (dartboard.isDouble(pt) && standardDeviationDoubles != null) NormalDistribution(mean.toDouble(), standardDeviationDoubles) else distribution

    private fun generateAngle(pt: Point, dartboard: Dartboard): Double
    {
        if (dartboard.isDouble(pt) || standardDeviationCentral == null)
        {
            //Just pluck a number from 0-360.
            return generateRandomAngle()
        }

        //Otherwise, we have a Normal Distribution to use to generate an angle more likely to be into the dartboard (rather than out of it)
        val angleToAvoid = getAngleForPoint(pt, dartboard.centerPoint)
        val angleTowardsCenter = (angleToAvoid + 180) % 360
        val angleDistribution = NormalDistribution(angleTowardsCenter, standardDeviationCentral)
        return angleDistribution.sample()
    }

    fun getProbabilityWithinRadius(radius: Double): Double
    {
        return distribution.probability(-radius, radius)
    }

    companion object
    {
        private const val SCORING_DARTS_TO_THROW = 20000
        private const val DOUBLE_DARTS_TO_THROW = 20000

        /**
         * Get the application-wide default thing to aim for, which applies to any score of 60 or less
         */
        fun getDefaultDartToAimAt(score: Int): Dart
        {
            //Aim for the single that puts you on double top
            if (score > 40)
            {
                val single = score - 40
                return Dart(single, 1)
            }

            //Aim for the double
            if (score % 2 == 0)
            {
                return Dart(score / 2, 2)
            }

            //On an odd number, less than 40. Aim to put ourselves on the highest possible power of 2.
            val scoreToLeaveRemaining = getHighestPowerOfTwoLessThan(score)
            val singleToAimFor = score - scoreToLeaveRemaining
            return Dart(singleToAimFor, 1)
        }

        private fun getHighestPowerOfTwoLessThan(score: Int): Int
        {
            var i = 2
            while (i < score)
            {
                i *= 2
            }

            return i / 2
        }
    }
}
