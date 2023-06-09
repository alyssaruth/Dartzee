package dartzee.utils

import dartzee.ai.AbstractSimulationRunner
import dartzee.ai.SimulationRunner
import dartzee.dartzee.AbstractDartzeeCalculator
import dartzee.dartzee.AbstractDartzeeRuleFactory
import dartzee.dartzee.AbstractDartzeeSegmentFactory
import dartzee.dartzee.AbstractDartzeeTemplateFactory
import dartzee.dartzee.DartzeeAimCalculator
import dartzee.dartzee.DartzeeCalculator
import dartzee.dartzee.DartzeeRuleFactory
import dartzee.dartzee.DartzeeSegmentFactory
import dartzee.dartzee.DartzeeTemplateFactory
import dartzee.game.GameLauncher
import dartzee.logging.LogDestinationSystemOut
import dartzee.logging.Logger
import dartzee.logging.LoggerFactory
import dartzee.logging.LoggingConsole
import dartzee.screen.ai.AISetupRuleFactory
import dartzee.screen.ai.AbstractAISetupRuleFactory
import dartzee.sync.AmazonS3RemoteDatabaseStore
import dartzee.sync.IRemoteDatabaseStore
import dartzee.sync.SYNC_BUCKET_NAME
import dartzee.sync.SyncConfigurer
import dartzee.sync.SyncManager
import java.time.Clock

object InjectedThings
{
    var allowModalDialogs = true
    var connectionPoolSize = 5
    var databaseDirectory = DATABASE_FILE_PATH
    var mainDatabase: Database = Database()
    var dartzeeCalculator: AbstractDartzeeCalculator = DartzeeCalculator()
    var dartzeeRuleFactory: AbstractDartzeeRuleFactory = DartzeeRuleFactory()
    var dartzeeTemplateFactory: AbstractDartzeeTemplateFactory = DartzeeTemplateFactory()
    var dartzeeSegmentFactory: AbstractDartzeeSegmentFactory = DartzeeSegmentFactory()
    var clock: Clock = Clock.systemUTC()
    val loggingConsole = LoggingConsole()
    var esDestination = LoggerFactory.constructElasticsearchDestination()
    var logger: Logger = Logger(listOf(loggingConsole, LogDestinationSystemOut(), esDestination))
    var gameLauncher: GameLauncher = GameLauncher()
    var dartzeeAimCalculator: DartzeeAimCalculator = DartzeeAimCalculator()
    var aiSetupRuleFactory: AbstractAISetupRuleFactory = AISetupRuleFactory()
    var simulationRunner: AbstractSimulationRunner = SimulationRunner()
    private val remoteDatabaseStore: IRemoteDatabaseStore = AmazonS3RemoteDatabaseStore(SYNC_BUCKET_NAME)
    var syncConfigurer: SyncConfigurer = SyncConfigurer(remoteDatabaseStore)
    var syncManager: SyncManager = SyncManager(remoteDatabaseStore)
}