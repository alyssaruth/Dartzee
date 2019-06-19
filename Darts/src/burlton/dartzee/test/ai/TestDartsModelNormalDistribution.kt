package burlton.dartzee.test.ai

import burlton.core.code.util.XmlUtil
import burlton.dartzee.code.ai.*
import burlton.dartzee.test.helper.AbstractDartsTest
import io.kotlintest.matchers.doubles.shouldBeBetween
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Test

class TestDartsModelNormalDistribution: AbstractDartsTest()
{
    /**
     * Read / write XML etc
     */
    @Test
    fun `Should not write optional values that are unset`()
    {
        val xmlDoc = XmlUtil.factoryNewDocument()!!
        val rootElement = xmlDoc.createElement("Test")

        val model = DartsModelNormalDistribution()
        model.standardDeviation = 25.6
        model.radiusAverageCount = 2

        model.writeXmlSpecific(rootElement)

        rootElement.getAttribute(ATTRIBUTE_STANDARD_DEVIATION) shouldBe "25.6"
        rootElement.getAttribute(ATTRIBUTE_RADIUS_AVERAGE_COUNT) shouldBe "2"
        rootElement.getAttribute(ATTRIBUTE_STANDARD_DEVIATION_CENTRAL) shouldBe ""
        rootElement.getAttribute(ATTRIBUTE_STANDARD_DEVIATION_DOUBLES) shouldBe ""
    }

    @Test
    fun `Should write optional values that have been set`()
    {
        val xmlDoc = XmlUtil.factoryNewDocument()!!
        val rootElement = xmlDoc.createElement("Test")

        val model = DartsModelNormalDistribution()
        model.standardDeviation = 13.0
        model.radiusAverageCount = 1
        model.standardDeviationCentral = 19.2
        model.standardDeviationDoubles = 3.7

        model.writeXmlSpecific(rootElement)

        rootElement.getAttribute(ATTRIBUTE_STANDARD_DEVIATION) shouldBe "13.0"
        rootElement.getAttribute(ATTRIBUTE_RADIUS_AVERAGE_COUNT) shouldBe "1"
        rootElement.getAttribute(ATTRIBUTE_STANDARD_DEVIATION_CENTRAL) shouldBe "19.2"
        rootElement.getAttribute(ATTRIBUTE_STANDARD_DEVIATION_DOUBLES) shouldBe "3.7"
    }

    @Test
    fun `Should handle optional values not being present in XML`()
    {
        val xmlDoc = XmlUtil.factoryNewDocument()!!
        val rootElement = xmlDoc.createElement("Test")

        rootElement.setAttribute(ATTRIBUTE_STANDARD_DEVIATION, "100.4")
        rootElement.setAttribute(ATTRIBUTE_RADIUS_AVERAGE_COUNT, "3")

        val model = DartsModelNormalDistribution()
        model.readXmlSpecific(rootElement)

        model.standardDeviation shouldBe 100.4
        model.radiusAverageCount shouldBe 3
        model.standardDeviationDoubles shouldBe 0.0
        model.standardDeviationCentral shouldBe 0.0

        model.distribution shouldNotBe null
        model.distribution!!.standardDeviation shouldBe 100.4
        model.distribution!!.mean shouldBe 0.0

        model.distributionDoubles shouldBe null
    }

    @Test
    fun `Should read optional values if present in XML`()
    {
        val xmlDoc = XmlUtil.factoryNewDocument()!!
        val rootElement = xmlDoc.createElement("Test")

        rootElement.setAttribute(ATTRIBUTE_STANDARD_DEVIATION, "100.4")
        rootElement.setAttribute(ATTRIBUTE_RADIUS_AVERAGE_COUNT, "3")
        rootElement.setAttribute(ATTRIBUTE_STANDARD_DEVIATION_DOUBLES, "50.2")
        rootElement.setAttribute(ATTRIBUTE_STANDARD_DEVIATION_CENTRAL, "43.5")

        val model = DartsModelNormalDistribution()
        model.readXmlSpecific(rootElement)

        model.standardDeviation shouldBe 100.4
        model.radiusAverageCount shouldBe 3
        model.standardDeviationDoubles shouldBe 50.2
        model.standardDeviationCentral shouldBe 43.5

        model.distribution shouldNotBe null
        model.distribution!!.standardDeviation shouldBe 100.4
        model.distribution!!.mean shouldBe 0.0

        model.distributionDoubles shouldNotBe null
        model.distributionDoubles!!.standardDeviation shouldBe 50.2
        model.distributionDoubles!!.mean shouldBe 0.0
    }

    @Test
    fun `Should return the correct density based on the standard deviation`()
    {
        val model = DartsModelNormalDistribution()
        model.populate(20.0, 0.0, 0.0, 1)

        //P(within 0.5 SD)
        model.getProbabilityWithinRadius(10.0).shouldBeBetween(0.3829, 0.3831, 0.0)

        //P(within 1 SD)
        model.getProbabilityWithinRadius(20.0).shouldBeBetween(0.6826, 0.6827, 0.0)

        //P(within 2 SD)
        model.getProbabilityWithinRadius(40.0).shouldBeBetween(0.9543, 0.9545, 0.0)

        //P(within 3 SD)
        model.getProbabilityWithinRadius(60.0).shouldBeBetween(0.9973, 0.9975, 0.0)
    }

    /**
     * Actual sampling behaviour
     */

}