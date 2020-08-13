package dartzee.screen.ai

import dartzee.ai.DartsAiModel
import dartzee.core.bean.NumberField
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSlider
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder

class AIConfigurationPanelNormalDistribution : AbstractAIConfigurationSubPanel(), ActionListener
{
    private val panelNorth = JPanel()
    private val lblStandardDeviation = JLabel("Standard Deviation")
    val nfStandardDeviation = NumberField(1)
    private val cbStandardDeviationDoubles = JCheckBox("Standard Deviation (Doubles)")
    val nfStandardDeviationDoubles = NumberField(1)
    private val cbCenterBias = JCheckBox("Standard Deviation (skew towards center)")
    val nfCentralBias = NumberField(1, 200)
    private val sliderPanel = JPanel()
    private val lblConsistent = JLabel("Consistent")
    private val lblErratic = JLabel("Erratic")
    private val slider = JSlider()

    init
    {
        layout = BorderLayout(0, 0)
        panelNorth.border = TitledBorder(null, "Variables", TitledBorder.LEADING, TitledBorder.TOP, null, null)
        add(panelNorth, BorderLayout.CENTER)
        panelNorth.layout = MigLayout("", "[125px,grow][100px,grow]", "[25px][][][][grow]")
        lblStandardDeviation.border = EmptyBorder(0, 5, 0, 0)
        lblStandardDeviation.preferredSize = Dimension(125, 25)
        panelNorth.add(lblStandardDeviation, "cell 0 0,alignx left,aligny top")
        nfStandardDeviation.preferredSize = Dimension(100, 25)
        nfStandardDeviation.value = 100
        panelNorth.add(nfStandardDeviation, "cell 1 0,alignx left,aligny top")
        panelNorth.add(cbStandardDeviationDoubles, "cell 0 1")
        nfStandardDeviationDoubles.preferredSize = Dimension(100, 25)
        panelNorth.add(nfStandardDeviationDoubles, "cell 1 1,alignx left,aligny top")
        panelNorth.add(cbCenterBias, "cell 0 2")
        nfCentralBias.preferredSize = Dimension(100, 25)
        panelNorth.add(nfCentralBias, "cell 1 2")

        slider.minimum = 50
        slider.maximum = 500
        panelNorth.add(sliderPanel, "cell 0 3, spanx, growx")
        sliderPanel.add(lblConsistent)
        sliderPanel.add(slider)
        sliderPanel.add(lblErratic)

        cbStandardDeviationDoubles.addActionListener(this)
        cbCenterBias.addActionListener(this)
    }

    fun initialiseModel(): DartsAiModel
    {
        val model = DartsAiModel.new()

        val sd = nfStandardDeviation.getDouble()
        val sdDoubles = if (cbStandardDeviationDoubles.isSelected) nfStandardDeviationDoubles.getDouble() else null
        val sdCentral = if (cbCenterBias.isSelected) nfCentralBias.getDouble() else null
        val maxOutlierRatio = slider.value.toDouble() / 100

        return model.copy(standardDeviation = sd,
            standardDeviationDoubles = sdDoubles,
            standardDeviationCentral = sdCentral,
            maxOutlierRatio = maxOutlierRatio)
    }

    override fun populateModel(model: DartsAiModel) = model

    override fun initialiseFromModel(model: DartsAiModel)
    {
        val standardDeviation = model.standardDeviation
        nfStandardDeviation.value = standardDeviation

        val standardDeviationDoubles = model.standardDeviationDoubles
        if (standardDeviationDoubles != null)
        {
            cbStandardDeviationDoubles.isSelected = true
            nfStandardDeviationDoubles.isEnabled = true
            nfStandardDeviationDoubles.value = standardDeviationDoubles
        }
        else
        {
            cbStandardDeviationDoubles.isSelected = false
            nfStandardDeviationDoubles.isEnabled = false
            nfStandardDeviationDoubles.value = 50
        }

        val sdCentral = model.standardDeviationCentral
        if (sdCentral != null)
        {
            cbCenterBias.isSelected = true
            nfCentralBias.isEnabled = true
            nfCentralBias.value = sdCentral
        }
        else
        {
            cbCenterBias.isSelected = false
            nfCentralBias.isEnabled = false
            nfCentralBias.value = 50
        }

        slider.value = (model.maxOutlierRatio * 100).toInt()
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        when (arg0.source)
        {
            cbStandardDeviationDoubles -> nfStandardDeviationDoubles.isEnabled = cbStandardDeviationDoubles.isSelected
            cbCenterBias -> nfCentralBias.isEnabled = cbCenterBias.isSelected
        }
    }

}