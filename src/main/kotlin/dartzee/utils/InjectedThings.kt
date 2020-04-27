package dartzee.utils

import dartzee.`object`.GameLauncher
import dartzee.dartzee.*
import dartzee.logging.LogDestinationSystemOut
import dartzee.logging.Logger
import dartzee.logging.LoggerFactory
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
    var esDestination = LoggerFactory.constructElasticsearchDestination()
    var logger: Logger = Logger(listOf(ScreenCache.loggingConsole, LogDestinationSystemOut(), esDestination))
    var gameLauncher: GameLauncher = GameLauncher()
    var terminator: ITerminator = Terminator()
}