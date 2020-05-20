package dartzee.ai

import dartzee.`object`.*
import dartzee.core.obj.HashMapCount
import dartzee.core.util.*
import dartzee.logging.CODE_SIMULATION_FINISHED
import dartzee.logging.CODE_SIMULATION_STARTED
import dartzee.screen.Dartboard
import dartzee.utils.InjectedThings.logger
import dartzee.utils.getAverage
import org.w3c.dom.Element
import java.awt.Point
import java.util.*

abstract class AbstractDartsModel
{
    var scoringDart = 20

    //X01
    var hmScoreToDart = mutableMapOf<Int, Dart>()
    var mercyThreshold = -1

    //Golf
    var hmDartNoToSegmentType = mutableMapOf<Int, SegmentType>()
    var hmDartNoToStopThreshold = mutableMapOf<Int, Int>()

    /**
     * Abstract methods
     */
    abstract fun getModelName(): String
    abstract fun getType(): Int
    abstract fun writeXmlSpecific(rootElement: Element)
    abstract fun readXmlSpecific(root: Element)
    abstract fun throwDartAtPoint(pt: Point, dartboard: Dartboard): Point
    abstract fun getProbabilityWithinRadius(radius: Double): Double

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

        readXmlSpecific(rootElement)
    }

    fun writeXml(): String
    {
        val xmlDoc = XmlUtil.factoryNewDocument()
        val rootElement = xmlDoc.createRootElement(getModelName())

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

    private fun throwScoringDart(dartboard: Dartboard): Point
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

    companion object
    {
        const val TYPE_NORMAL_DISTRIBUTION = 2

        const val DARTS_MODEL_NORMAL_DISTRIBUTION = "Simple Gaussian"

        const val TAG_SETUP_DART = "SetupDart"
        const val TAG_GOLF_AIM = "GolfAim"
        const val TAG_GOLF_STOP = "GolfStop"
        const val ATTRIBUTE_SCORING_DART = "ScoringDart"
        const val ATTRIBUTE_MERCY_RULE = "MercyRule"
        const val ATTRIBUTE_SCORE = "Score"
        const val ATTRIBUTE_DART_VALUE = "DartValue"
        const val ATTRIBUTE_DART_MULTIPLIER = "DartMultiplier"

        private const val SCORING_DARTS_TO_THROW = 20000
        private const val DOUBLE_DARTS_TO_THROW = 20000

        /**
         * Static methods
         */
        fun factoryForType(type: Int): AbstractDartsModel?
        {
            return when (type)
            {
                TYPE_NORMAL_DISTRIBUTION -> DartsModelNormalDistribution()
                else -> null
            }
        }

        fun getModelDescriptions(): Vector<String>
        {
            val models = Vector<String>()
            models.add(DARTS_MODEL_NORMAL_DISTRIBUTION)
            return models
        }

        fun getStrategyDesc(type: Int): String?
        {
            return when(type)
            {
                TYPE_NORMAL_DISTRIBUTION -> DARTS_MODEL_NORMAL_DISTRIBUTION
                else -> null
            }
        }


        /**
         * Get the application-wide default thing to aim for, which applies to any score of 60 or less
         */
        fun getDefaultDartToAimAt(score: Int): Dart
        {
            //Aim for the single that puts you on double top
            if (score > 40)
            {
                val single = score - 40
                return factorySingle(single)
            }

            //Aim for the double
            if (score % 2 == 0)
            {
                return factoryDouble(score / 2)
            }

            //On an odd number, less than 40. Aim to put ourselves on the highest possible power of 2.
            val scoreToLeaveRemaining = getHighestPowerOfTwoLessThan(score)
            val singleToAimFor = score - scoreToLeaveRemaining
            return factorySingle(singleToAimFor)
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
