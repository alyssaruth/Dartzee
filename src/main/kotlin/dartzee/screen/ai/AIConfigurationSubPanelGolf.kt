package dartzee.screen.ai

import dartzee.ai.AbstractDartsModel
import net.miginfocom.swing.MigLayout

class AIConfigurationSubPanelGolf : AbstractAIConfigurationSubPanel()
{
    private val panelDartOne = AIConfigurationGolfDartPanel(1)
    private val panelDartTwo = AIConfigurationGolfDartPanel(2)
    private val panelDartThree = AIConfigurationGolfDartPanel(3)

    init
    {
        layout = MigLayout("", "[grow]", "[grow][grow][grow]")

        add(panelDartOne, "cell 0 0,grow")
        add(panelDartTwo, "cell 0 1,grow")
        add(panelDartThree, "cell 0 2,grow")
    }

    override fun valid() = true

    override fun populateModel(model: AbstractDartsModel)
    {
        panelDartOne.populateModel(model)
        panelDartTwo.populateModel(model)
        panelDartThree.populateModel(model)
    }

    override fun initialiseFromModel(model: AbstractDartsModel)
    {
        panelDartOne.initialiseFromModel(model)
        panelDartTwo.initialiseFromModel(model)
        panelDartThree.initialiseFromModel(model)
    }
}
