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
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder

class AIConfigurationPanelNormalDistribution : AbstractAIConfigurationSubPanel(), ActionListener
{
    private val panelNorth = JPanel()
    private val lblStandardDeviation = JLabel("Standard Deviation")
    private val nfStandardDeviation = NumberField(1)
    private val cbStandardDeviationDoubles = JCheckBox("Standard Deviation (Doubles)")
    private val nfStandardDeviationDoubles = NumberField(1)
    private val nfCentralBias = NumberField(1, 200)
    private val cbCenterBias = JCheckBox("Standard Deviation (skew towards center)")

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

        cbStandardDeviationDoubles.addActionListener(this)
        cbCenterBias.addActionListener(this)
    }

    override fun valid() = true

    fun initialiseModel(): DartsAiModel
    {
        val model = DartsAiModel()

        val sd = nfStandardDeviation.getDouble()
        val sdDoubles = if (cbStandardDeviationDoubles.isSelected) nfStandardDeviationDoubles.getDouble() else 0.0
        val sdCentral = if(cbCenterBias.isSelected) nfCentralBias.getDouble() else 0.0

        model.populate(sd, sdDoubles, sdCentral)
        return model
    }

    override fun populateModel(model: DartsAiModel)
    {
        //Do nothing (we initialise the model instead)
    }

    override fun initialiseFromModel(model: DartsAiModel)
    {
        val standardDeviation = model.standardDeviation
        nfStandardDeviation.value = standardDeviation

        val standardDeviationDoubles = model.standardDeviationDoubles
        if (standardDeviationDoubles > 0)
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
        if (sdCentral > 0)
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