package dartzee.screen.ai

import dartzee.ai.AimDart

abstract class AbstractAISetupRuleFactory
{
    abstract fun newSetupRule(currentRules: MutableMap<Int, AimDart>)
}

class AISetupRuleFactory: AbstractAISetupRuleFactory()
{
    override fun newSetupRule(currentRules: MutableMap<Int, AimDart>)
    {
        NewSetupRuleDialog.addNewSetupRule(currentRules)
    }
}