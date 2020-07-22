package dartzee.screen.ai

import com.github.alexburlton.swingtest.getChild
import dartzee.`object`.SegmentType
import dartzee.ai.AbstractDartsModel
import dartzee.core.bean.ComboBoxItem
import dartzee.core.bean.items
import dartzee.core.bean.selectedItemTyped
import dartzee.helper.AbstractTest
import io.kotlintest.shouldBe
import org.junit.Test
import javax.swing.JComboBox
import javax.swing.JSpinner
import javax.swing.border.TitledBorder

class TestAIConfigurationSubPanelGolf: AbstractTest()
{
    @Test
    fun `Should initialise from a model correctly`()
    {
        val model = AbstractDartsModel()
        model.hmDartNoToSegmentType[1] = SegmentType.TREBLE
        model.hmDartNoToSegmentType[2] = SegmentType.INNER_SINGLE
        model.hmDartNoToSegmentType[3] = SegmentType.OUTER_SINGLE

        model.hmDartNoToStopThreshold[1] = 2
        model.hmDartNoToStopThreshold[2] = 4

        val panel = AIConfigurationSubPanelGolf()
        panel.initialiseFromModel(model)

        val dartOnePanel = panel.getPanelForDartNo(1)
        dartOnePanel.getChild<JComboBox<ComboBoxItem<SegmentType>>>().selectedItemTyped().hiddenData shouldBe SegmentType.TREBLE
        dartOnePanel.getChild<JSpinner>().value shouldBe 2

        val dartTwoPanel = panel.getPanelForDartNo(2)
        dartTwoPanel.getChild<JComboBox<ComboBoxItem<SegmentType>>>().selectedItemTyped().hiddenData shouldBe SegmentType.INNER_SINGLE
        dartTwoPanel.getChild<JSpinner>().value shouldBe 4

        val dartThreePanel = panel.getPanelForDartNo(3)
        dartThreePanel.getChild<JComboBox<ComboBoxItem<SegmentType>>>().selectedItemTyped().hiddenData shouldBe SegmentType.OUTER_SINGLE
    }

    @Test
    fun `Should populate the model correctly`()
    {
        val panel = AIConfigurationSubPanelGolf()

        panel.selectSegmentType(1, SegmentType.DOUBLE)
        panel.selectSegmentType(2, SegmentType.DOUBLE)
        panel.selectSegmentType(3, SegmentType.TREBLE)

        panel.getPanelForDartNo(1).getChild<JSpinner>().value = 1
        panel.getPanelForDartNo(2).getChild<JSpinner>().value = 2

        val model = AbstractDartsModel()
        panel.populateModel(model)

        model.hmDartNoToStopThreshold[1] shouldBe 1
        model.hmDartNoToStopThreshold[2] shouldBe 2

        model.hmDartNoToSegmentType[1] shouldBe SegmentType.DOUBLE
        model.hmDartNoToSegmentType[2] shouldBe SegmentType.DOUBLE
        model.hmDartNoToSegmentType[3] shouldBe SegmentType.TREBLE
    }

    @Test
    fun `Should always be valid`()
    {
        AIConfigurationSubPanelGolf().valid() shouldBe true
    }


    private fun AIConfigurationSubPanelGolf.getPanelForDartNo(dartNo: Int)
            = getChild<AIConfigurationGolfDartPanel> { (it.border as TitledBorder).title == "Dart #$dartNo" }

    private fun AIConfigurationSubPanelGolf.selectSegmentType(dartNo: Int, segmentType: SegmentType)
    {
        val panel = getPanelForDartNo(dartNo)
        val comboBox = panel.getChild<JComboBox<ComboBoxItem<SegmentType>>>()
        comboBox.items().find { it.hiddenData == segmentType }?.let { comboBox.selectedItem = it }
    }
}