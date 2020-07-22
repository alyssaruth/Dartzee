package dartzee.ai

import dartzee.`object`.Dart
import dartzee.`object`.SegmentType
import dartzee.`object`.getSegmentTypeForClockType
import dartzee.core.obj.HashMapCount
import dartzee.core.util.*
import dartzee.logging.CODE_AI_ERROR
import dartzee.logging.CODE_SIMULATION_FINISHED
import dartzee.logging.CODE_SIMULATION_STARTED
import dartzee.logging.LoggingCode
import dartzee.screen.Dartboard
import dartzee.screen.dartzee.DartzeeDartboard
import dartzee.screen.game.dartzee.SegmentStatus
import dartzee.utils.*
import dartzee.utils.InjectedThings.logger
import org.apache.commons.math3.distribution.NormalDistribution
import org.w3c.dom.Element
import java.awt.Point
import java.util.*

enum class DartzeePlayStyle {
    CAUTIOUS,
    AGGRESSIVE
}

class AbstractDartsModel
{
    //Modelling
    private val mean = 0

    var standardDeviation = 50.0
    var standardDeviationDoubles = 0.0
    var standardDeviationCentral = 0.0

    var distribution: NormalDistribution? = null
    var distributionDoubles: NormalDistribution? = null

    //X01
    var scoringDart = 20
    var hmScoreToDart = mutableMapOf<Int, Dart>()
    var mercyThreshold = -1

    //Golf
    var hmDartNoToSegmentType = mutableMapOf<Int, SegmentType>()
    var hmDartNoToStopThreshold = mutableMapOf<Int, Int>()

    //Dartzee
    var dartzeePlayStyle = DartzeePlayStyle.CAUTIOUS

    /**
     * Non-abstract stuff
     */
    fun readXml(xmlStr: String)
    {
        val xmlDoc = xmlStr.toXmlDoc()
        val rootElement = xmlDoc!!.documentElement

        val scoringSingle = rootElement.getAttributeInt(ATTRIBUTE_SCORING_DART)
        if (scoringSingle > 0)
        {
            this.scoringDart = scoringSingle
        }

        //X01
        mercyThreshold = rootElement.getAttributeInt(ATTRIBUTE_MERCY_RULE, -1)

        hmScoreToDart = mutableMapOf()
        val setupDarts = rootElement.getElementsByTagName(TAG_SETUP_DART)
        for (i in 0 until setupDarts.length)
        {
            val setupDart = setupDarts.item(i) as Element
            val score = setupDart.getAttributeInt(ATTRIBUTE_SCORE)
            val value = setupDart.getAttributeInt(ATTRIBUTE_DART_VALUE)
            val multiplier = setupDart.getAttributeInt(ATTRIBUTE_DART_MULTIPLIER)

            hmScoreToDart[score] = Dart(value, multiplier)
        }

        //Golf
        val hmDartNoToString = rootElement.readIntegerHashMap(TAG_GOLF_AIM)
        hmDartNoToSegmentType = hmDartNoToString.mapValues { SegmentType.valueOf(it.value) }.toMutableMap()
        hmDartNoToStopThreshold = rootElement.readIntegerHashMap(TAG_GOLF_STOP).mapValues { it.value.toInt() }.toMutableMap()

        //Dartzee
        val dartzeePlayStyleStr = rootElement.getAttribute(ATTRIBUTE_DARTZEE_PLAY_STYLE)
        dartzeePlayStyle = if (dartzeePlayStyleStr.isEmpty()) DartzeePlayStyle.CAUTIOUS else DartzeePlayStyle.valueOf(dartzeePlayStyleStr)

        readXmlSpecific(rootElement)
    }

    fun readXmlOldWay(xmlStr: String)
    {
        try
        {
            var fixed = xmlStr.replace("DartNumber", "Key", ignoreCase = true)
            fixed = fixed.replace("SegmentType", "Value", ignoreCase = true)
            fixed = fixed.replace("StopThreshold", "Value", ignoreCase = true)

            val xmlDoc = fixed.toXmlDoc()
            val rootElement = xmlDoc!!.documentElement

            val scoringSingle = rootElement.getAttributeInt(ATTRIBUTE_SCORING_DART)
            if (scoringSingle > 0)
            {
                this.scoringDart = scoringSingle
            }

            //X01
            mercyThreshold = rootElement.getAttributeInt(ATTRIBUTE_MERCY_RULE, -1)

            hmScoreToDart = mutableMapOf()
            val setupDarts = rootElement.getElementsByTagName(TAG_SETUP_DART)
            for (i in 0 until setupDarts.length)
            {
                val setupDart = setupDarts.item(i) as Element
                val score = setupDart.getAttributeInt(ATTRIBUTE_SCORE)
                val value = setupDart.getAttributeInt(ATTRIBUTE_DART_VALUE)
                val multiplier = setupDart.getAttributeInt(ATTRIBUTE_DART_MULTIPLIER)

                hmScoreToDart[score] = Dart(value, multiplier)
            }

            //Golf
            val hmDartNoToSegmentInt = rootElement.readIntegerHashMap(TAG_GOLF_AIM).mapValues { it.value.toInt() }
            hmDartNoToSegmentType = hmDartNoToSegmentInt.mapValues { DartsDatabaseUtil.convertOldSegmentType(it.value) }.toMutableMap()
            hmDartNoToStopThreshold = rootElement.readIntegerHashMap(TAG_GOLF_STOP).mapValues { it.value.toInt() }.toMutableMap()

            readXmlSpecific(rootElement)
        }
        catch (t: Throwable)
        {
            logger.error(LoggingCode("conversion.fucked"), xmlStr, t)
        }
    }

    fun writeXml(): String
    {
        val xmlDoc = XmlUtil.factoryNewDocument()
        val rootElement = xmlDoc.createRootElement("Gaussian")

        if (scoringDart != 20)
        {
            rootElement.setAttribute(ATTRIBUTE_SCORING_DART, "" + scoringDart)
        }

        hmScoreToDart.forEach { (score, drt) ->
            val child = xmlDoc.createElement(TAG_SETUP_DART)
            child.setAttribute(ATTRIBUTE_SCORE, "" + score)
            child.setAttribute(ATTRIBUTE_DART_VALUE, "" + drt.score)
            child.setAttribute(ATTRIBUTE_DART_MULTIPLIER, "" + drt.multiplier)
            rootElement.appendChild(child)
        }

        rootElement.writeHashMap(hmDartNoToSegmentType, TAG_GOLF_AIM)
        rootElement.writeHashMap(hmDartNoToStopThreshold, TAG_GOLF_STOP)

        if (mercyThreshold > -1)
        {
            rootElement.setAttribute(ATTRIBUTE_MERCY_RULE, "" + mercyThreshold)
        }

        rootElement.setAttribute(ATTRIBUTE_DARTZEE_PLAY_STYLE, "$dartzeePlayStyle")

        writeXmlSpecific(rootElement)

        return xmlDoc.toXmlString()
    }

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

    /**
     * Given the single/double/treble required, calculate the physical coordinates of the optimal place to aim
     */
    fun getPointForScore(drt: Dart, dartboard: Dartboard): Point
    {
        val score = drt.score
        val segmentType = drt.getSegmentTypeToAimAt()
        return getPointForScore(score, dartboard, segmentType)
    }

    private fun getPointForScore(score: Int, dartboard: Dartboard, type: SegmentType): Point
    {
        val points = dartboard.getPointsForSegment(score, type)
        val avgPoint = getAverage(points)

        //Need to rationalise here as we may have adjusted outside of the bounds
        //Shouldn't need this anymore, but can't hurt to leave it here anyway!
        dartboard.rationalisePoint(avgPoint)

        return avgPoint
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
    fun setSegmentTypeForDartNo(dartNo: Int, segmentType: SegmentType)
    {
        hmDartNoToSegmentType[dartNo] = segmentType
    }

    fun getStopThresholdForDartNo(dartNo: Int) = hmDartNoToStopThreshold.getOrDefault(dartNo, getDefaultStopThreshold(dartNo))
    fun setStopThresholdForDartNo(dartNo: Int, stopThreshold: Int)
    {
        hmDartNoToStopThreshold[dartNo] = stopThreshold
    }

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

    fun writeXmlSpecific(rootElement: Element)
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
    }

    fun readXmlSpecific(root: Element)
    {
        val sd = root.getAttributeDouble(ATTRIBUTE_STANDARD_DEVIATION)
        val sdDoubles = root.getAttributeDouble(ATTRIBUTE_STANDARD_DEVIATION_DOUBLES)
        val sdCentral = root.getAttributeDouble(ATTRIBUTE_STANDARD_DEVIATION_CENTRAL)

        populate(sd, sdDoubles, sdCentral)
    }

    fun getProbabilityWithinRadius(radius: Double): Double
    {
        return distribution!!.probability(-radius, radius)
    }

    fun populate(standardDeviation: Double, standardDeviationDoubles: Double, standardDeviationCentral: Double)
    {
        this.standardDeviation = standardDeviation
        this.standardDeviationDoubles = standardDeviationDoubles
        this.standardDeviationCentral = standardDeviationCentral

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

    companion object
    {
        const val DARTS_MODEL_NORMAL_DISTRIBUTION = "Simple Gaussian"

        const val ATTRIBUTE_STANDARD_DEVIATION = "StandardDeviation"
        const val ATTRIBUTE_STANDARD_DEVIATION_DOUBLES = "StandardDeviationDoubles"
        const val ATTRIBUTE_STANDARD_DEVIATION_CENTRAL = "StandardDeviationCentral"

        const val TAG_SETUP_DART = "SetupDart"
        const val TAG_GOLF_AIM = "GolfAim"
        const val TAG_GOLF_STOP = "GolfStop"
        const val ATTRIBUTE_SCORING_DART = "ScoringDart"
        const val ATTRIBUTE_MERCY_RULE = "MercyRule"
        const val ATTRIBUTE_SCORE = "Score"
        const val ATTRIBUTE_DART_VALUE = "DartValue"
        const val ATTRIBUTE_DART_MULTIPLIER = "DartMultiplier"
        const val ATTRIBUTE_DARTZEE_PLAY_STYLE = "DartzeePlayStyle"

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
