package dartzee.utils

import dartzee.`object`.GameLauncher
import dartzee.dartzee.*
import dartzee.logging.Logger
import dartzee.screen.ScreenCache
import java.time.Clock

object InjectedThings
{
    var dartzeeCalculator: AbstractDartzeeCalculator = DartzeeCalculator()
    var verificationDartboardSize = 400
    var dartzeeRuleFactory: AbstractDartzeeRuleFactory = DartzeeRuleFactory()
    var dartzeeTemplateFactory: AbstractDartzeeTemplateFactory = DartzeeTemplateFactory()
    var dartzeeSegmentFactory: AbstractDartzeeSegmentFactory = DartzeeSegmentFactory()
    var clock: Clock = Clock.systemUTC()
    var logger: Logger = Logger(listOf(ScreenCache.loggingConsole))
    var gameLauncher: GameLauncher = GameLauncher()
}