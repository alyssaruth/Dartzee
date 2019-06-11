package burlton.dartzee.code.screen.ai

import burlton.dartzee.code.ai.AbstractDartsModel
import burlton.dartzee.code.ai.DartsModelNormalDistribution
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
