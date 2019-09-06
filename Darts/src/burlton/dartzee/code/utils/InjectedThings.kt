package burlton.dartzee.code.utils

import burlton.dartzee.code.dartzee.AbstractDartzeeCalculator
import burlton.dartzee.code.dartzee.DartzeeCalculator

object InjectedThings
{
    var dartzeeCalculator: AbstractDartzeeCalculator = DartzeeCalculator()
}