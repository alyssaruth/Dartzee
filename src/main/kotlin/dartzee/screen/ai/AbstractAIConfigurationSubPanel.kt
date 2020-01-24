package dartzee.screen.ai

import dartzee.ai.AbstractDartsModel
import dartzee.ai.DartsModelNormalDistribution
import javax.swing.JPanel

abstract class AbstractAIConfigurationSubPanel : JPanel()
{
    abstract fun valid(): Boolean
    abstract fun populateModel(model: AbstractDartsModel)
    abstract fun initialiseFromModel(model: AbstractDartsModel)

    fun reset()
    {
        initialiseFromModel(DartsModelNormalDistribution())
    }
}
