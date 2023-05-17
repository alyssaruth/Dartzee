package dartzee.ai

import com.fasterxml.jackson.module.kotlin.readValue
import dartzee.core.util.jsonMapper
import dartzee.game.ClockType
import dartzee.logging.CODE_AI_ERROR
import dartzee.`object`.ComputedPoint
import dartzee.`object`.SegmentType
import dartzee.`object`.getSegmentTypeForClockType
import dartzee.screen.game.dartzee.SegmentStatuses
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.logger
import dartzee.utils.generateRandomAngle
import dartzee.utils.getAngleForPoint
import dartzee.utils.translatePoint
import getComputedPointForScore
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
    fun throwX01Dart(score: Int): ComputedPoint
    {
        //Check for a specific dart to aim for. It's possible to override any value for a specific AI strategy.
        val drtToAimAt = getOveriddenDartToAimAt(score)
        if (drtToAimAt != null)
        {
            val ptToAimAt = getPointForScore(drtToAimAt)
            return throwDartAtPoint(ptToAimAt)
        }

        //No overridden strategy, do the default thing
        return if (score > 60) {
            throwScoringDart()
        } else {
            val defaultDrt = getDefaultDartToAimAt(score)
            val ptToAimAt = getPointForScore(defaultDrt)
            throwDartAtPoint(ptToAimAt)
        }
    }

    fun throwScoringDart(): ComputedPoint
    {
        val ptToAimAt = calculateScoringPoint()
        return throwDartAtPoint(ptToAimAt.pt)
    }

    fun calculateScoringPoint(): ComputedPoint
    {
        val segmentType = if (scoringDart == 25) SegmentType.DOUBLE else SegmentType.TREBLE
        return getComputedPointForScore(scoringDart, segmentType)
    }

    private fun getOveriddenDartToAimAt(score: Int) = hmScoreToDart[score]

    /**
     * Golf
     */
    fun throwGolfDart(targetHole: Int, dartNo: Int): ComputedPoint
    {
        val segmentTypeToAimAt = getSegmentTypeForDartNo(dartNo)
        val ptToAimAt = getPointForScore(targetHole, segmentTypeToAimAt)
        return throwDartAtPoint(ptToAimAt)
    }

    /**
     * Clock
     */
    fun throwClockDart(clockTarget: Int, clockType: ClockType): ComputedPoint
    {
        val segmentType = getSegmentTypeForClockType(clockType)

        val ptToAimAt = getPointForScore(clockTarget, segmentType)
        return throwDartAtPoint(ptToAimAt)
    }

    /**
     * Dartzee
     */
    fun throwDartzeeDart(dartsThrownSoFar: Int, segmentStatuses: SegmentStatuses): ComputedPoint
    {
        val aggressive = (dartsThrownSoFar < 2 || dartzeePlayStyle == DartzeePlayStyle.AGGRESSIVE)
        val ptToAimAt = InjectedThings.dartzeeAimCalculator.getPointToAimFor(AI_DARTBOARD, segmentStatuses, aggressive)
        return throwDartAtPoint(ptToAimAt)
    }

    fun getSegmentTypeForDartNo(dartNo: Int) = hmDartNoToSegmentType.getValue(dartNo)

    fun getStopThresholdForDartNo(dartNo: Int) = hmDartNoToStopThreshold.getValue(dartNo)

    fun throwAtDouble(double: Int) = throwDartAtPoint(getPointForScore(double, SegmentType.DOUBLE))

    private fun throwDartAtPoint(aiDartboardPoint: Point): ComputedPoint
    {
        if (standardDeviation == 0.0)
        {
            logger.error(CODE_AI_ERROR, "Gaussian model with SD of 0 - this shouldn't be possible!")
            return AI_DARTBOARD.toComputedPoint(aiDartboardPoint)
        }

        if (aiDartboardPoint == DELIBERATE_MISS)
        {
            return AI_DARTBOARD.getDeliberateMissPoint()
        }

        val (radius, angle) = calculateRadiusAndAngle(aiDartboardPoint)
        val newPoint = translatePoint(aiDartboardPoint, radius, angle)
        return AI_DARTBOARD.toComputedPoint(newPoint)
    }

    data class DistributionSample(val radius: Double, val theta: Double)
    fun calculateRadiusAndAngle(pt: Point): DistributionSample
    {
        //Averaging logic
        val radius = sampleRadius(pt)

        //Generate the angle
        val theta = generateAngle(pt)
        val sanitisedAngle = sanitiseAngle(theta)

        return DistributionSample(radius, sanitisedAngle)
    }
    private fun sampleRadius(pt: Point): Double
    {
        val distribution = getDistributionToUse(pt)

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

    private fun getDistributionToUse(pt: Point) =
        if (standardDeviationDoubles != null && AI_DARTBOARD.isDouble(pt)) NormalDistribution(mean.toDouble(), standardDeviationDoubles) else distribution

    private fun generateAngle(pt: Point): Double
    {
        if (standardDeviationCentral == null || AI_DARTBOARD.isDouble(pt))
        {
            //Just pluck a number from 0-360.
            return generateRandomAngle()
        }

        //Otherwise, we have a Normal Distribution to use to generate an angle more likely to be into the dartboard (rather than out of it)
        val angleToAvoid = getAngleForPoint(pt, AI_DARTBOARD.computeCenter())
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
