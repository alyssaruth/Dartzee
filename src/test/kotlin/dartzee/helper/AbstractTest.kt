package dartzee.helper

import dartzee.CURRENT_TIME
import dartzee.core.helper.TestMessageDialogFactory
import dartzee.core.util.DialogUtil
import dartzee.logging.*
import dartzee.screen.Dartboard
import dartzee.screen.ScreenCache
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.Database
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.mainDatabase
import io.kotlintest.shouldBe
import io.mockk.clearAllMocks
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import java.awt.Window
import java.time.Clock
import java.time.ZoneId
import javax.swing.SwingUtilities
import javax.swing.UIManager
import kotlin.test.assertNotNull

private var doneOneTimeSetup = false
private val logDestination = FakeLogDestination()
val logger = Logger(listOf(logDestination, LogDestinationSystemOut()))
private var checkedForExceptions = false

val TEST_DB_DIRECTORY = "Test/Databases"

abstract class AbstractTest
{
    private var doneClassSetup = false
    protected val dialogFactory = TestMessageDialogFactory()

    @Before
    fun oneTimeSetup()
    {
        if (!doneOneTimeSetup)
        {
            doOneTimeSetup()
            doneOneTimeSetup = true
        }

        if (!doneClassSetup)
        {
            doClassSetup()
            doneClassSetup = true
        }

        beforeEachTest()
    }

    private fun doOneTimeSetup()
    {
        DialogUtil.init(dialogFactory)

        Thread.setDefaultUncaughtExceptionHandler(LoggerUncaughtExceptionHandler())

        InjectedThings.databaseDirectory = TEST_DB_DIRECTORY
        InjectedThings.logger = logger
        InjectedThings.dartboardSize = 50
        InjectedThings.preferencesDartboardSize = 50
        InjectedThings.clock = Clock.fixed(CURRENT_TIME, ZoneId.of("UTC"))

        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel")
        mainDatabase = Database(inMemory = true)
        DartsDatabaseUtil.initialiseDatabase(mainDatabase)
    }

    open fun doClassSetup()
    {
        DialogUtil.init(dialogFactory)
        InjectedThings.esDestination = mockk(relaxed = true)
    }

    open fun beforeEachTest()
    {
        ScreenCache.emptyCache()
        dialogFactory.reset()
        clearLogs()
        clearAllMocks()

        mainDatabase.localIdGenerator.hmLastAssignedIdByTableName.clear()
        DartsDatabaseUtil.getAllEntitiesIncludingVersion().forEach { wipeTable(it.getTableName()) }
        InjectedThings.dartzeeCalculator = FakeDartzeeCalculator()

        //Clear cached dartboards
        Dartboard.appearancePreferenceChanged()

        logger.loggingContext.clear()
    }

    @After
    open fun afterEachTest()
    {
        if (!checkedForExceptions)
        {
            errorLogged() shouldBe false
        }

        val visibleWindows = Window.getWindows().filter { it.isVisible }
        if (visibleWindows.isNotEmpty())
        {
            SwingUtilities.invokeLater { visibleWindows.forEach { it.dispose() } }
        }

        checkedForExceptions = false
    }

    fun getLastLog() = getLogRecords().last()

    fun verifyLog(code: LoggingCode, severity: Severity = Severity.INFO): LogRecord
    {
        val record = getLogRecords().findLast { it.loggingCode == code && it.severity == severity }
        assertNotNull(record)

        if (severity == Severity.ERROR)
        {
            checkedForExceptions = true
        }

        return record
    }

    fun verifyNoLogs(code: LoggingCode)
    {
        getLogRecords().any { it.loggingCode == code } shouldBe false
    }

    fun errorLogged(): Boolean
    {
        checkedForExceptions = true
        return getLogRecords().any { it.severity == Severity.ERROR }
    }

    fun getLogRecords(): List<LogRecord>
    {
        logger.waitUntilLoggingFinished()
        return logDestination.logRecords.toList()
    }
    fun clearLogs()
    {
        logger.waitUntilLoggingFinished()
        logDestination.clear()
    }
}