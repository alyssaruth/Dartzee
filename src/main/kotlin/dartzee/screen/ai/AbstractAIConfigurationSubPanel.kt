package dartzee.screen.ai

import dartzee.ai.DartsAiModelMk2
import javax.swing.JPanel

abstract class AbstractAIConfigurationSubPanel : JPanel()
{
    abstract fun valid(): Boolean
    abstract fun populateModel(model: DartsAiModelMk2): DartsAiModelMk2
    abstract fun initialiseFromModel(model: DartsAiModelMk2)

    fun reset()
    {
        initialiseFromModel(DartsAiModelMk2.new())
    }
}
