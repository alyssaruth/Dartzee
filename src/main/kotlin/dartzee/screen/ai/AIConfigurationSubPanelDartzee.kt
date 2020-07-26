package dartzee.screen.ai

import dartzee.ai.DartsAiModel
import dartzee.ai.DartzeePlayStyle
import dartzee.core.bean.RadioButtonPanel
import javax.swing.JRadioButton

class AIConfigurationSubPanelDartzee : AbstractAIConfigurationSubPanel()
{
    private val radioButtonPanel = RadioButtonPanel()
    private val rdbtnCautious = JRadioButton("Cautious")
    private val rdbtnAggressive = JRadioButton("Aggressive")

    init
    {
        add(radioButtonPanel)
        radioButtonPanel.add(rdbtnCautious)
        radioButtonPanel.add(rdbtnAggressive)
    }

    override fun valid() = true

    override fun populateModel(model: DartsAiModel)
    {
        model.dartzeePlayStyle = if (rdbtnCautious.isSelected) DartzeePlayStyle.CAUTIOUS else DartzeePlayStyle.AGGRESSIVE
    }

    override fun initialiseFromModel(model: DartsAiModel)
    {
        rdbtnCautious.isSelected = model.dartzeePlayStyle == DartzeePlayStyle.CAUTIOUS
        rdbtnAggressive.isSelected = model.dartzeePlayStyle == DartzeePlayStyle.AGGRESSIVE
    }
}