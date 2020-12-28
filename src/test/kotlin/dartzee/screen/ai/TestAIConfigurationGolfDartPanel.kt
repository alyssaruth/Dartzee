package dartzee.screen.ai

import com.github.alexburlton.swingtest.*
import dartzee.`object`.SegmentType
import dartzee.core.bean.ComboBoxItem
import dartzee.core.bean.items
import dartzee.core.bean.selectedItemTyped
import dartzee.helper.AbstractTest
import dartzee.helper.makeDartsModel
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JSpinner

class TestAIConfigurationGolfDartPanel: AbstractTest()
{
    @Test
    fun `Should hide the spinner for dart 3, but not the others`()
    {
        AIConfigurationGolfDartPanel(1).getChild<JSpinner>().parent.shouldBeVisible()
        AIConfigurationGolfDartPanel(2).getChild<JSpinner>().parent.shouldBeVisible()
        AIConfigurationGolfDartPanel(3).getChild<JSpinner>().parent.shouldNotBeVisible()
    }

    @Test
    fun `Should not populate the map for stop threshold if dart 3`()
    {
        val panel = AIConfigurationGolfDartPanel(3)
        panel.getChild<JSpinner>().value = 2

        val hmDartNoToStopThreshold = mutableMapOf<Int, Int>()
        panel.populateMaps(mutableMapOf(), hmDartNoToStopThreshold)
        hmDartNoToStopThreshold.size shouldBe 0
    }

    @Test
    fun `Should disable the 'or better' label appropriately`()
    {
        val panel = AIConfigurationGolfDartPanel(1)
        panel.getChild<JSpinner>().value = 2
        panel.getChild<JLabel>("or better").shouldBeEnabled()

        panel.getChild<JSpinner>().value = 1
        panel.getChild<JLabel>("or better").shouldBeDisabled()
    }

    @Test
    fun `Should initialise from a model correctly`()
    {
        val hmDartNoToSegmentType = mapOf(1 to SegmentType.TREBLE, 2 to SegmentType.INNER_SINGLE)
        val hmDartNoToStopThreshold = mapOf(1 to 2, 2 to 4)
        val model = makeDartsModel(hmDartNoToSegmentType = hmDartNoToSegmentType, hmDartNoToStopThreshold = hmDartNoToStopThreshold)

        val dartOnePanel = AIConfigurationGolfDartPanel(1)
        dartOnePanel.initialiseFromModel(model)
        dartOnePanel.getChild<JComboBox<ComboBoxItem<SegmentType>>>().selectedItemTyped().hiddenData shouldBe SegmentType.TREBLE
        dartOnePanel.getChild<JSpinner>().value shouldBe 2

        val dartTwoPanel = AIConfigurationGolfDartPanel(2)
        dartTwoPanel.initialiseFromModel(model)
        dartTwoPanel.getChild<JComboBox<ComboBoxItem<SegmentType>>>().selectedItemTyped().hiddenData shouldBe SegmentType.INNER_SINGLE
        dartTwoPanel.getChild<JSpinner>().value shouldBe 4
    }

    @Test
    fun `Should populate model correctly`()
    {
        val panel = AIConfigurationGolfDartPanel(1)
        panel.getChild<JSpinner>().value = 2
        panel.selectSegmentType(SegmentType.DOUBLE)

        val hmDartNoToSegmentType =  mutableMapOf<Int, SegmentType>()
        val hmDartNoToStopThreshold = mutableMapOf<Int, Int>()

        panel.populateMaps(hmDartNoToSegmentType, hmDartNoToStopThreshold)
        hmDartNoToSegmentType[1] shouldBe SegmentType.DOUBLE
        hmDartNoToStopThreshold[1] shouldBe 2

        panel.getChild<JSpinner>().value = 3
        panel.selectSegmentType(SegmentType.TREBLE)
        panel.populateMaps(hmDartNoToSegmentType, hmDartNoToStopThreshold)
        hmDartNoToSegmentType[1] shouldBe SegmentType.TREBLE
        hmDartNoToStopThreshold[1] shouldBe 3
    }


    private fun AIConfigurationGolfDartPanel.selectSegmentType(segmentType: SegmentType)
    {
        val comboBox = getChild<JComboBox<ComboBoxItem<SegmentType>>>()
        comboBox.items().find { it.hiddenData == segmentType }?.let { comboBox.selectedItem = it }
    }
}