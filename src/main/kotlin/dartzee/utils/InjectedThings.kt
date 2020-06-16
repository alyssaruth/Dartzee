package dartzee.utils

import dartzee.`object`.GameLauncher
import dartzee.dartzee.*
import dartzee.logging.LogDestinationSystemOut
import dartzee.logging.Logger
import dartzee.logging.LoggerFactory
import dartzee.logging.LoggingConsole
import dartzee.player.PlayerManager
import dartzee.screen.ChangeLog
import java.time.Clock

object InjectedThings
{
    var dartzeeCalculator: AbstractDartzeeCalculator = DartzeeCalculator()
    var verificationDartboardSize = 400
    var dartzeeRuleFactory: AbstractDartzeeRuleFactory = DartzeeRuleFactory()
    var dartzeeTemplateFactory: AbstractDartzeeTemplateFactory = DartzeeTemplateFactory()
    var dartzeeSegmentFactory: AbstractDartzeeSegmentFactory = DartzeeSegmentFactory()
    var clock: Clock = Clock.systemUTC()
    val loggingConsole = LoggingConsole()
    var esDestination = LoggerFactory.constructElasticsearchDestination()
    var logger: Logger = Logger(listOf(loggingConsole, LogDestinationSystemOut(), esDestination))
    var gameLauncher: GameLauncher = GameLauncher()
    var showChangeLog: () -> Unit = { ChangeLog().also { it.isVisible = true }}
    var playerManager: PlayerManager = PlayerManager()
    var dartzeeAimCalculator: DartzeeAimCalculator = DartzeeAimCalculator()
}