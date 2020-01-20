package burlton.dartzee.code.screen.ai

import burlton.dartzee.code.ai.AbstractDartsModel
import burlton.dartzee.code.ai.DartsModelNormalDistribution
import burlton.dartzee.code.core.bean.NumberField
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder

class AIConfigurationPanelNormalDistribution : AbstractAIConfigurationPanel(), ActionListener
{
    private val panelNorth = JPanel()
    private val lblStandardDeviation = JLabel("Standard Deviation")
    private val nfStandardDeviation = NumberField(1)
    private val cbStandardDeviationDoubles = JCheckBox("Standard Deviation (Doubles)")
    private val nfStandardDeviationDoubles = NumberField(1)
    private val nfCentralBias = NumberField(1, 200)
    private val cbCenterBias = JCheckBox("Standard Deviation (skew towards center)")
    private val cbRadiusAverage = JCheckBox("Radius as average of multiple throws")
    private val spinnerAverageCount = JSpinner()

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
        panelNorth.add(cbRadiusAverage, "cell 0 3")
        panelNorth.add(spinnerAverageCount, "cell 1 3")
        spinnerAverageCount.model = SpinnerNumberModel(2, 2, 5, 1)

        cbStandardDeviationDoubles.addActionListener(this)
        cbCenterBias.addActionListener(this)
        cbRadiusAverage.addActionListener(this)
    }

    override fun valid() = true

    override fun initialiseModel(): AbstractDartsModel
    {
        val model = DartsModelNormalDistribution()

        val sd = nfStandardDeviation.getDouble()
        val sdDoubles = if (cbStandardDeviationDoubles.isSelected) nfStandardDeviationDoubles.getDouble() else 0.0
        val sdCentral = if(cbCenterBias.isSelected) nfCentralBias.getDouble() else 0.0

        val averageCount = if (cbRadiusAverage.isSelected) spinnerAverageCount.value as Int else 1

        model.populate(sd, sdDoubles, sdCentral, averageCount)
        return model
    }

    override fun initialiseFromModel(model: AbstractDartsModel)
    {
        val normalModel = model as DartsModelNormalDistribution
        val standardDeviation = normalModel.standardDeviation
        nfStandardDeviation.value = standardDeviation

        val standardDeviationDoubles = normalModel.standardDeviationDoubles
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

        val sdCentral = normalModel.standardDeviationCentral
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

        val avgCount = normalModel.radiusAverageCount
        cbRadiusAverage.isSelected = avgCount > 1
        spinnerAverageCount.isEnabled = avgCount > 1
        spinnerAverageCount.value = Math.max(2, avgCount)
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        when (arg0.source)
        {
            cbStandardDeviationDoubles -> nfStandardDeviationDoubles.isEnabled = cbStandardDeviationDoubles.isSelected
            cbCenterBias -> nfCentralBias.isEnabled = cbCenterBias.isSelected
            cbRadiusAverage -> spinnerAverageCount.isEnabled = cbRadiusAverage.isSelected
        }
    }

}