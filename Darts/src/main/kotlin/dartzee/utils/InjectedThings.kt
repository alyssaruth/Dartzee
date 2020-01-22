package dartzee.utils

import dartzee.dartzee.*

object InjectedThings
{
    var dartzeeCalculator: AbstractDartzeeCalculator = DartzeeCalculator()
    var verificationDartboardSize = 400
    var dartzeeRuleFactory: AbstractDartzeeRuleFactory = DartzeeRuleFactory()
    var dartzeeTemplateFactory: AbstractDartzeeTemplateFactory = DartzeeTemplateFactory()
    var dartzeeSegmentFactory: AbstractDartzeeSegmentFactory = DartzeeSegmentFactory()
}