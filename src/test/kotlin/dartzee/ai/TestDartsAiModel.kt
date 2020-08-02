package dartzee.ai

import dartzee.`object`.SegmentType
import dartzee.helper.AbstractTest
import io.kotlintest.matchers.maps.shouldContainExactly
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Test

class TestDartsAiModel: AbstractTest()
{
    /**
     * Serialisation
     */
    @Test
    fun `Should serialise and deserialise correctly with default values`()
    {
        val model = DartsAiModel()

        val xml = model.writeXml()

        val newModel = DartsAiModel()
        newModel.readXml(xml)

        model.scoringDart shouldBe newModel.scoringDart
        model.hmScoreToDart shouldContainExactly newModel.hmScoreToDart
        model.hmDartNoToSegmentType shouldContainExactly newModel.hmDartNoToSegmentType
        model.hmDartNoToStopThreshold shouldContainExactly newModel.hmDartNoToStopThreshold
        model.mercyThreshold shouldBe newModel.mercyThreshold
        model.standardDeviation shouldBe newModel.standardDeviation
        model.standardDeviationDoubles shouldBe newModel.standardDeviationDoubles
        model.standardDeviationCentral shouldBe newModel.standardDeviationCentral
        model.dartzeePlayStyle shouldBe DartzeePlayStyle.CAUTIOUS
    }

    @Test
    fun `Should serialise and deserialise non-defaults correctly`()
    {
        val model = DartsAiModel()
        model.scoringDart = 25
        model.hmScoreToDart[50] = AimDart(25, 2)
        model.hmScoreToDart[60] = AimDart(10, 1)
        model.hmDartNoToSegmentType[1] = SegmentType.TREBLE
        model.hmDartNoToStopThreshold[1] = 1
        model.hmDartNoToStopThreshold[2] = 2
        model.mercyThreshold = 18
        model.dartzeePlayStyle = DartzeePlayStyle.AGGRESSIVE
        model.standardDeviation = 25.6
        model.standardDeviationDoubles = 50.2
        model.standardDeviationCentral = 80.5

        val xml = model.writeXml()

        val newModel = DartsAiModel()
        newModel.readXml(xml)

        newModel.scoringDart shouldBe 25
        newModel.mercyThreshold shouldBe 18
        newModel.dartzeePlayStyle shouldBe DartzeePlayStyle.AGGRESSIVE
        newModel.standardDeviation shouldBe 25.6
        newModel.standardDeviationDoubles shouldBe 50.2
        newModel.standardDeviationCentral shouldBe 80.5
        newModel.hmScoreToDart shouldContainExactly model.hmScoreToDart
        newModel.hmDartNoToSegmentType shouldContainExactly model.hmDartNoToSegmentType
        newModel.hmDartNoToStopThreshold shouldContainExactly model.hmDartNoToStopThreshold

        newModel.distribution shouldNotBe null
        newModel.distribution!!.standardDeviation shouldBe 25.6
        newModel.distribution!!.mean shouldBe 0.0

        newModel.distributionDoubles shouldNotBe null
        newModel.distributionDoubles!!.standardDeviation shouldBe 50.2
        newModel.distributionDoubles!!.mean shouldBe 0.0
    }
}