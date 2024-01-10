package dartzee.screen.ai

import dartzee.ai.AimDart

interface IAISetupRuleFactory {
    fun newSetupRule(currentRules: MutableMap<Int, AimDart>)
}

class AISetupRuleFactory : IAISetupRuleFactory {
    override fun newSetupRule(currentRules: MutableMap<Int, AimDart>) {
        NewSetupRuleDialog.addNewSetupRule(currentRules)
    }
}
