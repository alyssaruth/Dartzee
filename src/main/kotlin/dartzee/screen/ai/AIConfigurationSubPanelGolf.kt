package dartzee.screen.ai

import dartzee.`object`.SegmentType
import dartzee.ai.DartsAiModel
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

    override fun populateModel(model: DartsAiModel): DartsAiModel
    {
        val hmDartNoToSegmentType =  mutableMapOf<Int, SegmentType>()
        val hmDartNoToStopThreshold = mutableMapOf<Int, Int>()

        panelDartOne.populateMaps(hmDartNoToSegmentType, hmDartNoToStopThreshold)
        panelDartTwo.populateMaps(hmDartNoToSegmentType, hmDartNoToStopThreshold)
        panelDartThree.populateMaps(hmDartNoToSegmentType, hmDartNoToStopThreshold)

        return model.copy(hmDartNoToSegmentType = hmDartNoToSegmentType.toMap(), hmDartNoToStopThreshold = hmDartNoToStopThreshold.toMap())
    }

    override fun initialiseFromModel(model: DartsAiModel)
    {
        panelDartOne.initialiseFromModel(model)
        panelDartTwo.initialiseFromModel(model)
        panelDartThree.initialiseFromModel(model)
    }
}
