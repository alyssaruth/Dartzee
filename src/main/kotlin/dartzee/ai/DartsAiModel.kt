package dartzee.ai

import dartzee.`object`.SegmentType
import dartzee.core.util.*
import org.apache.commons.math3.distribution.NormalDistribution
import org.w3c.dom.Element

class DartsAiModel
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
    var hmScoreToDart = mutableMapOf<Int, AimDart>()
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

            hmScoreToDart[score] = AimDart(value, multiplier)
        }

        //Golf
        val hmDartNoToString = rootElement.readIntegerHashMap(TAG_GOLF_AIM)
        hmDartNoToSegmentType = hmDartNoToString.mapValues { SegmentType.valueOf(it.value) }.toMutableMap()
        hmDartNoToStopThreshold = rootElement.readIntegerHashMap(TAG_GOLF_STOP).mapValues { it.value.toInt() }.toMutableMap()

        //Dartzee
        val dartzeePlayStyleStr = rootElement.getAttribute(ATTRIBUTE_DARTZEE_PLAY_STYLE)
        dartzeePlayStyle = if (dartzeePlayStyleStr.isEmpty()) DartzeePlayStyle.CAUTIOUS else DartzeePlayStyle.valueOf(dartzeePlayStyleStr)

        val sd = rootElement.getAttributeDouble(ATTRIBUTE_STANDARD_DEVIATION)
        val sdDoubles = rootElement.getAttributeDouble(ATTRIBUTE_STANDARD_DEVIATION_DOUBLES)
        val sdCentral = rootElement.getAttributeDouble(ATTRIBUTE_STANDARD_DEVIATION_CENTRAL)

        populate(sd, sdDoubles, sdCentral)
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

        rootElement.setAttribute(ATTRIBUTE_STANDARD_DEVIATION, "" + standardDeviation)

        if (standardDeviationDoubles > 0)
        {
            rootElement.setAttribute(ATTRIBUTE_STANDARD_DEVIATION_DOUBLES, "" + standardDeviationDoubles)
        }

        if (standardDeviationCentral > 0)
        {
            rootElement.setAttribute(ATTRIBUTE_STANDARD_DEVIATION_CENTRAL, "" + standardDeviationCentral)
        }

        return xmlDoc.toXmlString()
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
    }
}
