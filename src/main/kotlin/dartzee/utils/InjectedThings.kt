package dartzee.utils

import dartzee.`object`.GameLauncher
import dartzee.ai.AbstractSimulationRunner
import dartzee.ai.SimulationRunner
import dartzee.dartzee.*
import dartzee.logging.LogDestinationSystemOut
import dartzee.logging.Logger
import dartzee.logging.LoggerFactory
import dartzee.logging.LoggingConsole
import dartzee.player.PlayerManager
import dartzee.screen.ChangeLog
import dartzee.screen.IPlayerImageSelector
import dartzee.screen.PlayerImageDialog
import dartzee.screen.ai.AISetupRuleFactory
import dartzee.screen.ai.AbstractAISetupRuleFactory
import dartzee.sync.*
import java.time.Clock

object InjectedThings
{
    var databaseDirectory = DATABASE_FILE_PATH
    var mainDatabase: Database = Database()
    var dartzeeCalculator: AbstractDartzeeCalculator = DartzeeCalculator()
    var dartboardSize = 400
    var preferencesDartboardSize = 450
    var dartzeeRuleFactory: AbstractDartzeeRuleFactory = DartzeeRuleFactory()
    var dartzeeTemplateFactory: AbstractDartzeeTemplateFactory = DartzeeTemplateFactory()
    var dartzeeSegmentFactory: AbstractDartzeeSegmentFactory = DartzeeSegmentFactory()
    var playerImageSelector: IPlayerImageSelector = PlayerImageDialog()
    var clock: Clock = Clock.systemUTC()
    val loggingConsole = LoggingConsole()
    var esDestination = LoggerFactory.constructElasticsearchDestination()
    var logger: Logger = Logger(listOf(loggingConsole, LogDestinationSystemOut(), esDestination))
    var gameLauncher: GameLauncher = GameLauncher()
    var showChangeLog: () -> Unit = { ChangeLog().also { it.isVisible = true }}
    var playerManager: PlayerManager = PlayerManager()
    val dartzeeAimCalculator: DartzeeAimCalculator = DartzeeAimCalculator()
    var aiSetupRuleFactory: AbstractAISetupRuleFactory = AISetupRuleFactory()
    var simulationRunner: AbstractSimulationRunner = SimulationRunner()
    var remoteDatabaseStore: IRemoteDatabaseStore = AmazonS3RemoteDatabaseStore(SYNC_BUCKET_NAME)
    var syncConfigurer: SyncConfigurer = SyncConfigurer(remoteDatabaseStore)
    var syncManager: SyncManager = SyncManager(remoteDatabaseStore)
}