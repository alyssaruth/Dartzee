package dartzee.screen.ai

import dartzee.ai.DartsAiModel
import javax.swing.JPanel

abstract class AbstractAIConfigurationSubPanel : JPanel()
{
    abstract fun valid(): Boolean
    abstract fun populateModel(model: DartsAiModel): DartsAiModel
    abstract fun initialiseFromModel(model: DartsAiModel)

    fun reset()
    {
        initialiseFromModel(DartsAiModel.new())
    }
}
