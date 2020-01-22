package dartzee.ai

import dartzee.core.util.Debug
import dartzee.core.util.getAttributeDouble
import dartzee.core.util.getAttributeInt
import dartzee.screen.Dartboard
import dartzee.utils.generateRandomAngle
import dartzee.utils.getAngleForPoint
import dartzee.utils.translatePoint
import org.apache.commons.math3.distribution.NormalDistribution
import org.w3c.dom.Element
import java.awt.Point

const val ATTRIBUTE_STANDARD_DEVIATION = "StandardDeviation"
const val ATTRIBUTE_STANDARD_DEVIATION_DOUBLES = "StandardDeviationDoubles"
const val ATTRIBUTE_STANDARD_DEVIATION_CENTRAL = "StandardDeviationCentral"
const val ATTRIBUTE_RADIUS_AVERAGE_COUNT = "RadiusAverageCount"

class DartsModelNormalDistribution : AbstractDartsModel()
{
    private val mean = 0

    var standardDeviation = 50.0
    var standardDeviationDoubles = -1.0
    var standardDeviationCentral = -1.0
    var radiusAverageCount = 1

    var distribution: NormalDistribution? = null
    var distributionDoubles: NormalDistribution? = null

    override fun throwDartAtPoint(pt: Point, dartboard: Dartboard): Point
    {
        Debug.append("Throwing dart at $pt", LOGGING)
        if (standardDeviation == 0.0)
        {
            Debug.stackTrace("Gaussian model with SD of 0 - this shouldn't be possible!")
            return pt
        }

        val (radius, angle) = calculateRadiusAndAngle(pt, dartboard)

        Debug.appendWithoutDate("Radius = $radius, theta = $angle", LOGGING)

        return translatePoint(pt, radius, angle, LOGGING)
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
        var radius = 0.0
        for (i in 0 until radiusAverageCount)
        {
            val distribution = getDistributionToUse(pt, dartboard)
            radius += Math.abs(distribution!!.sample())
        }

        return radius / radiusAverageCount
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

    private fun getDistributionToUse(pt: Point, dartboard: Dartboard): NormalDistribution?
    {
        return if (dartboard.isDouble(pt) && distributionDoubles != null) distributionDoubles else distribution
    }

    private fun generateAngle(pt: Point, dartboard: Dartboard): Double
    {
        if (dartboard.isDouble(pt) || standardDeviationCentral == 0.0)
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

    override fun getModelName() = "Gaussian"
    override fun getType() = TYPE_NORMAL_DISTRIBUTION

    override fun writeXmlSpecific(rootElement: Element)
    {
        rootElement.setAttribute(ATTRIBUTE_STANDARD_DEVIATION, "" + standardDeviation)

        if (standardDeviationDoubles > 0)
        {
            rootElement.setAttribute(ATTRIBUTE_STANDARD_DEVIATION_DOUBLES, "" + standardDeviationDoubles)
        }

        if (standardDeviationCentral > 0)
        {
            rootElement.setAttribute(ATTRIBUTE_STANDARD_DEVIATION_CENTRAL, "" + standardDeviationCentral)
        }

        rootElement.setAttribute(ATTRIBUTE_RADIUS_AVERAGE_COUNT, "" + radiusAverageCount)
    }

    override fun readXmlSpecific(root: Element)
    {
        val sd = root.getAttributeDouble(ATTRIBUTE_STANDARD_DEVIATION)
        val sdDoubles = root.getAttributeDouble(ATTRIBUTE_STANDARD_DEVIATION_DOUBLES)
        val sdCentral = root.getAttributeDouble(ATTRIBUTE_STANDARD_DEVIATION_CENTRAL)
        val radiusAverageCount = root.getAttributeInt(ATTRIBUTE_RADIUS_AVERAGE_COUNT, 1)

        populate(sd, sdDoubles, sdCentral, radiusAverageCount)
    }

    override fun getProbabilityWithinRadius(radius: Double): Double
    {
        return distribution!!.probability(-radius, radius)
    }

    fun populate(standardDeviation: Double, standardDeviationDoubles: Double, standardDeviationCentral: Double,
                 radiusAverageCount: Int)
    {
        this.standardDeviation = standardDeviation
        this.standardDeviationDoubles = standardDeviationDoubles
        this.standardDeviationCentral = standardDeviationCentral
        this.radiusAverageCount = radiusAverageCount

        distribution = NormalDistribution(mean.toDouble(), standardDeviation)
        if (standardDeviationDoubles > 0)
        {
            distributionDoubles = NormalDistribution(mean.toDouble(), standardDeviationDoubles)
        }
        else
        {
            distributionDoubles = null
        }
    }
}
