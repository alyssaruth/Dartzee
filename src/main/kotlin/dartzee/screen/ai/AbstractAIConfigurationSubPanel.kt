package dartzee.screen.ai

import dartzee.ai.AbstractDartsModel
import javax.swing.JPanel

abstract class AbstractAIConfigurationSubPanel : JPanel()
{
    abstract fun valid(): Boolean
    abstract fun populateModel(model: AbstractDartsModel)
    abstract fun initialiseFromModel(model: AbstractDartsModel)

    fun reset()
    {
        initialiseFromModel(AbstractDartsModel())
    }
}
