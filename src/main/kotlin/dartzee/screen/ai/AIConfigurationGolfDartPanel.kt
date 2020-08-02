package dartzee.screen.ai

import dartzee.`object`.SegmentType
import dartzee.ai.DartsAiModelMk2
import dartzee.core.bean.ComboBoxItem
import dartzee.core.bean.items
import dartzee.core.bean.selectedItemTyped
import javax.swing.*
import javax.swing.border.TitledBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

class AIConfigurationGolfDartPanel(private val dartNo: Int) : JPanel(), ChangeListener
{
    private val comboBox = JComboBox<ComboBoxItem<SegmentType>>()
    private val panelStoppingPoint = JPanel()
    private val spinner = JSpinner()
    private val lblOrBelow = JLabel("or better")

    init
    {
        border = TitledBorder(null, "Dart #$dartNo", TitledBorder.LEADING, TitledBorder.TOP, null, null)

        val lblAimAt = JLabel("Aim at")
        add(lblAimAt)
        add(comboBox)
        val horizontalStrut = Box.createHorizontalStrut(20)
        add(horizontalStrut)
        val horizontalStrut2 = Box.createHorizontalStrut(20)
        add(horizontalStrut2)
        add(panelStoppingPoint)
        val lblStopIfScored = JLabel("Stop if scored ")
        panelStoppingPoint.add(lblStopIfScored)
        panelStoppingPoint.add(spinner)
        panelStoppingPoint.add(lblOrBelow)

        spinner.addChangeListener(this)

        setModels()
        setComponentVisibility()
    }


    private fun setModels()
    {
        comboBox.addItem(ComboBoxItem(SegmentType.DOUBLE, "Double (1)"))
        comboBox.addItem(ComboBoxItem(SegmentType.TREBLE, "Treble (2)"))
        comboBox.addItem(ComboBoxItem(SegmentType.INNER_SINGLE, "Inner Single (3)"))
        comboBox.addItem(ComboBoxItem(SegmentType.OUTER_SINGLE, "Outer Single (4)"))

        spinner.model = SpinnerNumberModel(2, 1, 4, 1)
    }

    private fun setComponentVisibility()
    {
        panelStoppingPoint.isVisible = dartNo < 3

        val value = spinner.value as Int
        lblOrBelow.isEnabled = value > 1
    }

    fun initialiseFromModel(model: DartsAiModelMk2)
    {
        //Combo box selection
        val segmentType = model.getSegmentTypeForDartNo(dartNo)
        val item = comboBox.items().find { it.hiddenData == segmentType }
        item?.let{ comboBox.selectedItem = it }

        if (spinner.isVisible)
        {
            val stopThreshold = model.getStopThresholdForDartNo(dartNo)
            spinner.value = stopThreshold
        }
    }

    fun populateMaps(hmDartNoToSegmentType: MutableMap<Int, SegmentType>, hmDartNoToStopThreshold: MutableMap<Int, Int>)
    {
        val item = comboBox.selectedItemTyped()
        val segmentType = item.hiddenData

        hmDartNoToSegmentType[dartNo] = segmentType

        if (spinner.isVisible)
        {
            val stopThreshold = spinner.value as Int
            hmDartNoToStopThreshold[dartNo] = stopThreshold
        }
    }

    override fun stateChanged(arg0: ChangeEvent)
    {
        setComponentVisibility()
    }
}
