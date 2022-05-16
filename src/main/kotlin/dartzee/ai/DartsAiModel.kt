package dartzee.ai

import com.fasterxml.jackson.module.kotlin.readValue
import dartzee.`object`.SegmentType
import dartzee.`object`.getSegmentTypeForClockType
import dartzee.core.util.jsonMapper
import dartzee.game.ClockType
import dartzee.logging.CODE_AI_ERROR
import dartzee.screen.Dartboard
import dartzee.screen.dartzee.DartzeeDartboard
import dartzee.screen.game.dartzee.SegmentStatus
import dartzee.utils.*
import dartzee.utils.InjectedThings.logger
import getDefaultDartToAimAt
import getPointForScore
import org.apache.commons.math3.distribution.NormalDistribution
import java.awt.Point
import kotlin.math.abs

enum class DartzeePlayStyle {
    CAUTIOUS,
    AGGRESSIVE
}

val DELIBERATE_MISS = Point(Int.MAX_VALUE, Int.MAX_VALUE)

data class DartsAiModel(val standardDeviation: Double,
                        val standardDeviationDoubles: Double?,
                        val standardDeviationCentral: Double?,
                        val maxRadius: Int,
                        val scoringDart: Int,
                        val hmScoreToDart: Map<Int, AimDart>,
                        val mercyThreshold: Int?,
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
        val ptToAimAt = getScoringPoint()
        return throwDartAtPoint(ptToAimAt, dartboard)
    }

    fun getScoringPoint(dartboard: Dartboard = AI_DARTBOARD): Point
    {
        val segmentType = if (scoringDart == 25) SegmentType.DOUBLE else SegmentType.TREBLE
        return getPointForScore(scoringDart, segmentType, dartboard)
    }

    private fun getOveriddenDartToAimAt(score: Int) = hmScoreToDart[score]

    /**
     * Golf
     */
    fun throwGolfDart(targetHole: Int, dartNo: Int, dartboard: Dartboard)
    {
        val segmentTypeToAimAt = getSegmentTypeForDartNo(dartNo)
        val ptToAimAt = getPointForScore(targetHole, segmentTypeToAimAt)
        val pt = throwDartAtPoint(ptToAimAt, dartboard)
        dartboard.dartThrown(pt)
    }

    /**
     * Clock
     */
    fun throwClockDart(clockTarget: Int, clockType: ClockType, dartboard: Dartboard)
    {
        val segmentType = getSegmentTypeForClockType(clockType)

        val ptToAimAt = getPointForScore(clockTarget, segmentType)
        val pt = throwDartAtPoint(ptToAimAt, dartboard)
        dartboard.dartThrown(pt)
    }

    /**
     * Dartzee
     */
    fun throwDartzeeDart(dartsThrownSoFar: Int, dartboard: DartzeeDartboard, segmentStatus: SegmentStatus)
    {
        val aggressive = (dartsThrownSoFar < 2 || dartzeePlayStyle == DartzeePlayStyle.AGGRESSIVE)
        val ptToAimAt = InjectedThings.dartzeeAimCalculator.getPointToAimFor(AI_DARTBOARD, segmentStatus, aggressive)
        val pt = throwDartAtPoint(ptToAimAt, dartboard)
        dartboard.dartThrown(pt)
    }

    fun getSegmentTypeForDartNo(dartNo: Int) = hmDartNoToSegmentType.getValue(dartNo)

    fun getStopThresholdForDartNo(dartNo: Int) = hmDartNoToStopThreshold.getValue(dartNo)

    fun throwAtDouble(double: Int, dartboard: Dartboard) = throwDartAtPoint(getPointForScore(double, SegmentType.DOUBLE), dartboard)

    private fun throwDartAtPoint(aiDartboardPoint: Point, destinationDartboard: Dartboard): Point
    {
        if (standardDeviation == 0.0)
        {
            logger.error(CODE_AI_ERROR, "Gaussian model with SD of 0 - this shouldn't be possible!")
            return aiDartboardPoint
        }

        if (aiDartboardPoint == DELIBERATE_MISS)
        {
            return destinationDartboard.getPointsForSegment(3, SegmentType.MISSED_BOARD).first()
        }

        val (radius, angle) = calculateRadiusAndAngle(aiDartboardPoint, AI_DARTBOARD)

        val resultingAiPoint = translatePoint(aiDartboardPoint, radius, angle)
        return convertForDestinationDartboard(resultingAiPoint, AI_DARTBOARD, destinationDartboard)
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
        val distribution = getDistributionToUse(pt, dartboard)

        var radius = distribution.sample()
        while (abs(radius) > maxRadius)
        {
            radius = distribution.sample()
        }

        return radius
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
        if (standardDeviationDoubles != null && dartboard.isDouble(pt)) NormalDistribution(mean.toDouble(), standardDeviationDoubles) else distribution

    private fun generateAngle(pt: Point, dartboard: Dartboard): Double
    {
        if (standardDeviationCentral == null || dartboard.isDouble(pt))
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

    fun getProbabilityWithinRadius(radius: Double): Double?
    {
        if (radius > maxRadius) return null

        return distribution.probability(-radius, radius)
    }

    fun computeProbabilityDensityDivisor(): Double
    {
        val maxPossible = maxRadius.toDouble()
        return distribution.probability(-maxPossible, maxPossible)
    }

    fun toJson(): String = jsonMapper().writeValueAsString(this)

    companion object
    {
        val DEFAULT_GOLF_SEGMENT_TYPES = mapOf(1 to SegmentType.DOUBLE, 2 to SegmentType.TREBLE, 3 to SegmentType.TREBLE)
        val DEFAULT_GOLF_STOP_THRESHOLDS = mapOf(1 to 2, 2 to 3)

        fun fromJson(json: String) = jsonMapper().readValue<DartsAiModel>(json)

        fun new(): DartsAiModel
        {
            val hmDartNoToSegmentType = DEFAULT_GOLF_SEGMENT_TYPES.toMutableMap()
            val hmDartNoToStopThreshold = DEFAULT_GOLF_STOP_THRESHOLDS.toMutableMap()
            return DartsAiModel(50.0,
                null,
                null,
                150,
                20,
                emptyMap(),
                null,
                hmDartNoToSegmentType,
                hmDartNoToStopThreshold,
                DartzeePlayStyle.CAUTIOUS)
        }
    }
}
