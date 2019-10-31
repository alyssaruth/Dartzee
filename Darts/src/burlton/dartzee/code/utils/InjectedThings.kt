package burlton.dartzee.code.utils

import burlton.dartzee.code.dartzee.AbstractDartzeeCalculator
import burlton.dartzee.code.dartzee.AbstractDartzeeRuleFactory
import burlton.dartzee.code.dartzee.DartzeeCalculator
import burlton.dartzee.code.dartzee.DartzeeRuleFactory

object InjectedThings
{
    var dartzeeCalculator: AbstractDartzeeCalculator = DartzeeCalculator()
    var verificationDartboardSize = 400
    var dartzeeRuleFactory: AbstractDartzeeRuleFactory = DartzeeRuleFactory()
}