package dartzee.screen.ai

import com.github.alexburlton.swingtest.getChild
import com.github.alexburlton.swingtest.shouldBeDisabled
import com.github.alexburlton.swingtest.shouldBeEnabled
import dartzee.`object`.SegmentType
import dartzee.ai.DartsAiModel
import dartzee.core.bean.ComboBoxItem
import dartzee.core.bean.items
import dartzee.core.bean.selectedItemTyped
import dartzee.helper.AbstractTest
import dartzee.shouldBeVisible
import dartzee.shouldNotBeVisible
import io.kotlintest.shouldBe
import org.junit.Test
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
        val model = DartsAiModel()
        model.hmDartNoToSegmentType[1] = SegmentType.TREBLE
        model.hmDartNoToSegmentType[2] = SegmentType.INNER_SINGLE

        model.hmDartNoToStopThreshold[1] = 2
        model.hmDartNoToStopThreshold[2] = 4

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
        val model = DartsAiModel()

        val panel = AIConfigurationGolfDartPanel(1)
        panel.getChild<JSpinner>().value = 2
        panel.selectSegmentType(SegmentType.DOUBLE)

        panel.populateModel(model)
        model.hmDartNoToSegmentType[1] shouldBe SegmentType.DOUBLE
        model.hmDartNoToStopThreshold[1] shouldBe 2

        panel.getChild<JSpinner>().value = 3
        panel.selectSegmentType(SegmentType.TREBLE)
        panel.populateModel(model)
        model.hmDartNoToSegmentType[1] shouldBe SegmentType.TREBLE
        model.hmDartNoToStopThreshold[1] shouldBe 3
    }


    private fun AIConfigurationGolfDartPanel.selectSegmentType(segmentType: SegmentType)
    {
        val comboBox = getChild<JComboBox<ComboBoxItem<SegmentType>>>()
        comboBox.items().find { it.hiddenData == segmentType }?.let { comboBox.selectedItem = it }
    }
}